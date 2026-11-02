package com.ntmreactors.tile;

import com.ntmreactors.NtmItems;
import com.ntmreactors.ReactorType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nullable;

/**
 * Lógica común de los 3 reactores (se ejecuta solo en el servidor).
 *
 * Ranuras: 0 = combustible, 1 = secundaria (refrigerante / activador / moderador), 2 = salida (combustible agotado).
 * Genera Forge Energy (FE/RF) y la empuja a los bloques vecinos que acepten energía.
 */
public abstract class TileEntityReactorBase extends TileEntity implements ITickable {

    public static final int SLOT_FUEL = 0, SLOT_AUX = 1, SLOT_OUT = 2;

    /** Cantidad de valores que se sincronizan al GUI (ver getSyncData). */
    public static final int SYNC_COUNT = 9;

    // --- Calor (solo reactor rápido). Valores por tick. ---
    public static final int HEAT_RISE = 3;     // calor que produce funcionando
    public static final int HEAT_PASSIVE = 1;  // disipación natural mientras funciona
    public static final int HEAT_COOLANT = 4;  // disipación extra con litio activo
    public static final int HEAT_IDLE = 2;     // enfriamiento cuando no funciona
    public static final int HEAT_SCRAM = 1000; // al llegar aquí el reactor se apaga
    public static final int HEAT_RESET = 200;  // por debajo de esto vuelve a arrancar

    public final ReactorType type;

    public final ItemStackHandler inventory;

    protected final ReactorEnergy energy;

    private int burnTime;       // ticks que le quedan al combustible actual
    private int burnMax;
    private int fuelPercent;    // % de potencia del combustible actual
    private String depletedId = "";
    private int auxTime;        // ticks que le quedan al refrigerante / moderador actual
    private int auxMax;
    private int heat;
    private boolean running;
    private boolean tripped;    // apagado por sobrecalentamiento
    private boolean ignited;    // reactor de torio: ya fue encendido con uranio/MOX
    private int currentOutput;
    private ItemStack pendingOutput = ItemStack.EMPTY;

    private final int[] clientData = new int[SYNC_COUNT];

    protected TileEntityReactorBase(ReactorType type) {
        this.type = type;
        this.energy = new ReactorEnergy(type.capacity, type.output * 4);
        this.inventory = new ItemStackHandler(3) {
            @Override
            protected void onContentsChanged(int slot) {
                markDirty();
            }

            // Solo deja entrar (jugador, tolvas, tuberías) lo que cada ranura acepta.
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                if (!TileEntityReactorBase.this.type.accepts(slot, stack)) return stack;
                return super.insertItem(slot, stack, simulate);
            }
        };
    }

    // ------------------------------------------------------------------ lógica

    @Override
    public void update() {
        if (world.isRemote) return;

        flushOutput();
        if (tripped && heat <= HEAT_RESET) tripped = false;

        // 1. Cargar combustible nuevo cuando se agota el actual
        if (burnTime <= 0) {
            burnMax = 0;
            fuelPercent = 0;
            if (!tripped && pendingOutput.isEmpty()) loadFuel();
            if (burnTime <= 0) ignited = false;
        }

        // 2. Ranura secundaria (litio / agua pesada). El torio solo la usa para encender.
        boolean fuelOk = burnTime > 0;
        boolean needsAux = type != ReactorType.THORIUM;
        if (fuelOk && needsAux && auxTime <= 0) loadAux();
        boolean auxActive = needsAux && auxTime > 0;
        boolean auxOk = type != ReactorType.HEAVY_WATER || auxActive; // el HWR no funciona sin moderador

        // 3. ¿Puede funcionar este tick?
        int gen = fuelOk ? type.output * fuelPercent / 100 : 0;
        boolean room = energy.getEnergyStored() + gen <= energy.getMaxEnergyStored(); // buffer lleno = en pausa
        boolean wasRunning = running;
        running = fuelOk && auxOk && room && !tripped;
        currentOutput = running ? gen : 0;

        if (running) {
            energy.add(gen);
            burnTime--;
            if (needsAux && auxTime > 0) auxTime--;
            if (type.usesHeat) {
                heat += HEAT_RISE - HEAT_PASSIVE - (auxActive ? HEAT_COOLANT : 0);
            }
            if (burnTime <= 0) finishFuel();
        } else if (type.usesHeat) {
            heat -= HEAT_IDLE;
        }

        if (type.usesHeat) {
            heat = Math.max(0, Math.min(HEAT_SCRAM, heat));
            if (heat >= HEAT_SCRAM) {
                tripped = true;
                running = false;
                currentOutput = 0;
            }
        }

        pushEnergy();
        if (running || wasRunning) markDirty();
    }

    private void loadFuel() {
        ItemStack stack = inventory.getStackInSlot(SLOT_FUEL);
        if (stack.isEmpty()) return;
        ReactorType.Fuel fuel = type.getFuel(stack);
        if (fuel == null) return;

        // Torio: para arrancar en frío hace falta uranio o MOX en la ranura secundaria (se gasta 1)
        if (type == ReactorType.THORIUM && !ignited) {
            ItemStack activator = inventory.getStackInSlot(SLOT_AUX);
            if (activator.isEmpty() || type.getAux(activator) == null) return;
            inventory.extractItem(SLOT_AUX, 1, false);
            ignited = true;
        }

        inventory.extractItem(SLOT_FUEL, 1, false);
        burnTime = fuel.burnTicks;
        burnMax = fuel.burnTicks;
        fuelPercent = fuel.outputPercent;
        depletedId = fuel.depleted == null ? "" : fuel.depleted;
    }

    private void loadAux() {
        ItemStack stack = inventory.getStackInSlot(SLOT_AUX);
        if (stack.isEmpty()) return;
        ReactorType.Aux aux = type.getAux(stack);
        if (aux == null) return;
        inventory.extractItem(SLOT_AUX, 1, false);
        auxTime = aux.ticks;
        auxMax = aux.ticks;
    }

    private void finishFuel() {
        burnTime = 0;
        ItemStack depleted = NtmItems.make(depletedId, 1);
        depletedId = "";
        if (!depleted.isEmpty()) pendingOutput = depleted;
        flushOutput();
    }

    /** Mueve el combustible agotado a la ranura de salida; si está llena, el reactor espera a que la vacíes. */
    private void flushOutput() {
        if (pendingOutput.isEmpty()) return;
        ItemStack out = inventory.getStackInSlot(SLOT_OUT);
        if (out.isEmpty()) {
            inventory.setStackInSlot(SLOT_OUT, pendingOutput);
            pendingOutput = ItemStack.EMPTY;
        } else if (ItemHandlerHelper.canItemStacksStack(out, pendingOutput)
                && out.getCount() + pendingOutput.getCount() <= out.getMaxStackSize()) {
            ItemStack merged = out.copy();
            merged.grow(pendingOutput.getCount());
            inventory.setStackInSlot(SLOT_OUT, merged);
            pendingOutput = ItemStack.EMPTY;
        }
    }

    private void pushEnergy() {
        for (EnumFacing face : EnumFacing.VALUES) {
            if (energy.getEnergyStored() <= 0) return;
            TileEntity neighbor = world.getTileEntity(pos.offset(face));
            if (neighbor == null) continue;
            IEnergyStorage target = neighbor.getCapability(CapabilityEnergy.ENERGY, face.getOpposite());
            if (target == null || !target.canReceive()) continue;
            int offered = energy.extractEnergy(type.output * 4, true);
            int accepted = target.receiveEnergy(offered, false);
            if (accepted > 0) energy.extractEnergy(accepted, false);
        }
    }

    // ------------------------------------------------------------ sincronización con el GUI

    /**
     * Valores para el GUI: 0-1 energía (parte baja/alta), 2 calor, 3 combustible restante, 4 combustible total,
     * 5 refrigerante/moderador restante, 6 total, 7 flags (1 = funcionando, 2 = SCRAM, 4 = encendido), 8 FE/tick.
     */
    public int[] getSyncData() {
        if (world != null && world.isRemote) return clientData;
        int e = energy.getEnergyStored();
        return new int[]{e & 0xFFFF, e >>> 16, heat, burnTime, burnMax, auxTime, auxMax,
                (running ? 1 : 0) | (tripped ? 2 : 0) | (ignited ? 4 : 0), currentOutput};
    }

    public void setSyncData(int index, int value) {
        if (index >= 0 && index < SYNC_COUNT) clientData[index] = value;
    }

    public int guiEnergy() {
        int[] d = getSyncData();
        return (d[0] & 0xFFFF) | (d[1] << 16);
    }

    public boolean isUsableByPlayer(EntityPlayer player) {
        return world.getTileEntity(pos) == this
                && player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }

    public void dropContents() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(i));
        }
        InventoryHelper.spawnItemStack(world, pos.getX(), pos.getY(), pos.getZ(), pendingOutput);
        pendingOutput = ItemStack.EMPTY;
    }

    // ------------------------------------------------------------------ capabilities

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return capability == CapabilityEnergy.ENERGY
                || capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY
                || super.hasCapability(capability, facing);
    }

    @Nullable
    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) return CapabilityEnergy.ENERGY.cast(energy);
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory);
        return super.getCapability(capability, facing);
    }

    // ------------------------------------------------------------------ NBT

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tag) {
        super.writeToNBT(tag);
        tag.setTag("Inventory", inventory.serializeNBT());
        tag.setInteger("Energy", energy.getEnergyStored());
        tag.setInteger("BurnTime", burnTime);
        tag.setInteger("BurnMax", burnMax);
        tag.setInteger("FuelPercent", fuelPercent);
        tag.setString("Depleted", depletedId);
        tag.setInteger("AuxTime", auxTime);
        tag.setInteger("AuxMax", auxMax);
        tag.setInteger("Heat", heat);
        tag.setBoolean("Tripped", tripped);
        tag.setBoolean("Ignited", ignited);
        tag.setTag("Pending", pendingOutput.writeToNBT(new NBTTagCompound()));
        return tag;
    }

    @Override
    public void readFromNBT(NBTTagCompound tag) {
        super.readFromNBT(tag);
        inventory.deserializeNBT(tag.getCompoundTag("Inventory"));
        energy.setStored(tag.getInteger("Energy"));
        burnTime = tag.getInteger("BurnTime");
        burnMax = tag.getInteger("BurnMax");
        fuelPercent = tag.getInteger("FuelPercent");
        depletedId = tag.getString("Depleted");
        auxTime = tag.getInteger("AuxTime");
        auxMax = tag.getInteger("AuxMax");
        heat = tag.getInteger("Heat");
        tripped = tag.getBoolean("Tripped");
        ignited = tag.getBoolean("Ignited");
        pendingOutput = new ItemStack(tag.getCompoundTag("Pending"));
    }

    // ------------------------------------------------------------------ energía

    /** Buffer que solo se puede vaciar desde fuera (el reactor genera, no recibe). */
    public static class ReactorEnergy extends EnergyStorage {
        public ReactorEnergy(int capacity, int maxExtract) {
            super(capacity, 0, maxExtract);
        }

        public void add(int amount) {
            energy = Math.min(capacity, energy + amount);
        }

        public void setStored(int amount) {
            energy = Math.max(0, Math.min(capacity, amount));
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}

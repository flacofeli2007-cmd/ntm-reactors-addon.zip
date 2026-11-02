package com.ntmreactors.gui;

import com.ntmreactors.tile.TileEntityReactorBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerReactor extends Container {

    private final TileEntityReactorBase tile;
    private final int[] cache = new int[TileEntityReactorBase.SYNC_COUNT];

    public ContainerReactor(InventoryPlayer playerInv, TileEntityReactorBase tile) {
        this.tile = tile;

        // 0 = combustible, 1 = secundaria (litio / activador / agua pesada), 2 = salida (solo sacar)
        addSlotToContainer(new SlotItemHandler(tile.inventory, TileEntityReactorBase.SLOT_FUEL, 56, 26));
        addSlotToContainer(new SlotItemHandler(tile.inventory, TileEntityReactorBase.SLOT_AUX, 56, 56));
        addSlotToContainer(new SlotItemHandler(tile.inventory, TileEntityReactorBase.SLOT_OUT, 140, 26));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlotToContainer(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlotToContainer(new Slot(playerInv, col, 8 + col * 18, 142));
        }
    }

    public TileEntityReactorBase getTile() {
        return tile;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        int[] now = tile.getSyncData();
        for (int i = 0; i < now.length; i++) {
            if (now[i] != cache[i]) {
                for (IContainerListener listener : listeners) {
                    listener.sendWindowProperty(this, i, now[i]);
                }
                cache[i] = now[i];
            }
        }
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void updateProgressBar(int id, int data) {
        tile.setSyncData(id, data & 0xFFFF); // el paquete lleva 16 bits
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tile.isUsableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            result = stack.copy();
            if (index < 3) {
                // del reactor al inventario del jugador
                if (!mergeItemStack(stack, 3, 39, true)) return ItemStack.EMPTY;
            } else if (!mergeItemStack(stack, 0, 2, false)) {
                // del jugador al reactor (solo entra lo que cada ranura acepta)
                return ItemStack.EMPTY;
            }
            if (stack.isEmpty()) slot.putStack(ItemStack.EMPTY);
            else slot.onSlotChanged();
            if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }
        return result;
    }
}

package com.ntmreactors;

import com.ntmreactors.tile.TileEntityFastReactor;
import com.ntmreactors.tile.TileEntityHeavyWaterReactor;
import com.ntmreactors.tile.TileEntityReactorBase;
import com.ntmreactors.tile.TileEntityThoriumReactor;
import net.minecraft.item.ItemStack;

/**
 * Datos de balance de cada reactor. Para ajustar el juego casi todo se cambia AQUÍ:
 * potencia (FE/tick), buffer de energía, combustibles aceptados y su duración.
 */
public enum ReactorType {

    // Reactor de Neutrones Rápidos: mucha potencia, necesita refrigerante de litio (slot 1) o se sobrecalienta.
    FAST("fast_reactor", 800, 400_000, true,
            new Fuel[]{
                    new Fuel("hbm:rod_uranium_fuel", 12000, 100, "hbm:rod_uranium_fuel_depleted"),
                    new Fuel("hbm:rod_mox_fuel", 12000, 150, "hbm:rod_mox_fuel_depleted")},
            new Aux[]{
                    new Aux("hbm:lithium", 1200),
                    new Aux("hbm:powder_lithium", 300)}),

    // Reactor de Torio (iniciación dual): barra de torio en la ranura base; uranio o MOX en la ranura secundaria
    // solo para arrancar (se consume 1 al encender). Una vez encendido sigue solo mientras haya torio.
    THORIUM("thorium_reactor", 300, 150_000, false,
            new Fuel[]{
                    new Fuel("hbm:rod_thorium_fuel", 24000, 100, "hbm:rod_thorium_fuel_depleted"),
                    new Fuel("hbm:rod_th232", 24000, 60, "hbm:rod_thorium_fuel_depleted")},
            new Aux[]{
                    new Aux("hbm:rod_uranium_fuel", 0),
                    new Aux("hbm:rod_mox_fuel", 0)}),

    // Reactor de Agua Pesada (HWR): quema uranio natural directo, pero necesita agua pesada como moderador (slot 1).
    HEAVY_WATER("heavy_water_reactor", 120, 60_000, false,
            new Fuel[]{
                    new Fuel("hbm:billet_uranium", 1000, 100, null),
                    new Fuel("hbm:ingot_uranium", 6000, 100, "hbm:ingot_u238"),
                    new Fuel("hbm:rod_uranium", 12000, 100, null)},
            new Aux[]{
                    new Aux("ntmreactors:heavy_water_cell", 6000)});

    /** Combustible: id del ítem, ticks que dura, % de potencia (100 = normal), ítem agotado que deja (o null). */
    public static final class Fuel {
        public final String item;
        public final int burnTicks;
        public final int outputPercent;
        public final String depleted;

        public Fuel(String item, int burnTicks, int outputPercent, String depleted) {
            this.item = item;
            this.burnTicks = burnTicks;
            this.outputPercent = outputPercent;
            this.depleted = depleted;
        }
    }

    /** Ítem de la ranura secundaria (refrigerante / activador / moderador) y cuántos ticks dura. */
    public static final class Aux {
        public final String item;
        public final int ticks;

        public Aux(String item, int ticks) {
            this.item = item;
            this.ticks = ticks;
        }
    }

    public final String id;
    public final int output;       // FE por tick a potencia 100%
    public final int capacity;     // buffer interno de energía (FE)
    public final boolean usesHeat; // solo el reactor rápido gestiona calor
    private final Fuel[] fuels;
    private final Aux[] auxes;

    ReactorType(String id, int output, int capacity, boolean usesHeat, Fuel[] fuels, Aux[] auxes) {
        this.id = id;
        this.output = output;
        this.capacity = capacity;
        this.usesHeat = usesHeat;
        this.fuels = fuels;
        this.auxes = auxes;
    }

    public Fuel getFuel(ItemStack stack) {
        for (Fuel f : fuels) if (NtmItems.is(stack, f.item)) return f;
        return null;
    }

    public Aux getAux(ItemStack stack) {
        for (Aux a : auxes) if (NtmItems.is(stack, a.item)) return a;
        return null;
    }

    /** Qué puede entrar en cada ranura: 0 = combustible, 1 = secundaria, 2 = salida (nada entra). */
    public boolean accepts(int slot, ItemStack stack) {
        if (slot == 0) return getFuel(stack) != null;
        if (slot == 1) return getAux(stack) != null;
        return false;
    }

    public TileEntityReactorBase createTile() {
        switch (this) {
            case FAST: return new TileEntityFastReactor();
            case THORIUM: return new TileEntityThoriumReactor();
            default: return new TileEntityHeavyWaterReactor();
        }
    }
}

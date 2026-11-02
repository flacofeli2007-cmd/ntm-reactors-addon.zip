package com.ntmreactors;

import net.minecraft.item.Item;

public final class ModItems {

    /** Moderador del HWR. Se craftea con deuterio de NTM + cubo de agua (ver recipes/heavy_water_cell.json). */
    public static final Item HEAVY_WATER_CELL = new Item()
            .setRegistryName(NtmReactors.MODID, "heavy_water_cell")
            .setUnlocalizedName(NtmReactors.MODID + ".heavy_water_cell")
            .setCreativeTab(NtmReactors.TAB);

    private ModItems() {}
}

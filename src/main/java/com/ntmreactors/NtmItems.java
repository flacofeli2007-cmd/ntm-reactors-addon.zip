package com.ntmreactors;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

/** Utilidades para trabajar con ítems de NTM sin compilar contra su jar (solo por nombre de registro). */
public final class NtmItems {

    private NtmItems() {}

    /** true si el stack es el ítem con ese id completo, p. ej. "hbm:rod_th232". */
    public static boolean is(ItemStack stack, String fullId) {
        if (stack.isEmpty()) return false;
        ResourceLocation name = stack.getItem().getRegistryName();
        return name != null && name.toString().equals(fullId);
    }

    /** Crea un stack por id; devuelve EMPTY si el ítem no existe (p. ej. NTM cambió el nombre). */
    public static ItemStack make(String fullId, int count) {
        if (fullId == null || fullId.isEmpty()) return ItemStack.EMPTY;
        Item item = Item.getByNameOrId(fullId);
        return item == null ? ItemStack.EMPTY : new ItemStack(item, count);
    }
}

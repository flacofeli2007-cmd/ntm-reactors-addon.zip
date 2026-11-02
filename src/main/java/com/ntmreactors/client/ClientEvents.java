package com.ntmreactors.client;

import com.ntmreactors.ModBlocks;
import com.ntmreactors.ModItems;
import com.ntmreactors.NtmReactors;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = NtmReactors.MODID, value = Side.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerModels(ModelRegistryEvent event) {
        for (Block block : ModBlocks.all()) {
            register(Item.getItemFromBlock(block));
        }
        register(ModItems.HEAVY_WATER_CELL);
    }

    private static void register(Item item) {
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }
}

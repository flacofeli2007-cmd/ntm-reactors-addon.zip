package com.ntmreactors;

import com.ntmreactors.gui.GuiHandler;
import com.ntmreactors.tile.TileEntityFastReactor;
import com.ntmreactors.tile.TileEntityHeavyWaterReactor;
import com.ntmreactors.tile.TileEntityThoriumReactor;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;

/**
 * Addon de NTM Extended (modid "hbm"): añade 3 reactores nucleares.
 * "required-after:hbm" obliga a que NTM se cargue antes que este mod.
 */
@Mod(modid = NtmReactors.MODID, name = NtmReactors.NAME, version = NtmReactors.VERSION,
        acceptedMinecraftVersions = "[1.12.2]", dependencies = "required-after:hbm")
public class NtmReactors {

    public static final String MODID = "ntmreactors";
    public static final String NAME = "NTM Reactors Addon";
    public static final String VERSION = "0.1.0";

    @Mod.Instance(MODID)
    public static NtmReactors instance;

    public static final CreativeTabs TAB = new CreativeTabs(MODID) {
        @Override
        public ItemStack getTabIconItem() {
            return new ItemStack(ModBlocks.THORIUM_REACTOR);
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerTileEntity(TileEntityFastReactor.class, new ResourceLocation(MODID, "fast_reactor"));
        GameRegistry.registerTileEntity(TileEntityThoriumReactor.class, new ResourceLocation(MODID, "thorium_reactor"));
        GameRegistry.registerTileEntity(TileEntityHeavyWaterReactor.class, new ResourceLocation(MODID, "heavy_water_reactor"));
        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    }
}

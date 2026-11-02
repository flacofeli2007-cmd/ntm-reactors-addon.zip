package com.ntmreactors.block;

import com.ntmreactors.NtmReactors;
import com.ntmreactors.ReactorType;
import com.ntmreactors.tile.TileEntityReactorBase;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/** Un solo bloque para los 3 reactores; el ReactorType decide qué TileEntity crea. */
public class BlockReactor extends Block {

    private final ReactorType type;

    public BlockReactor(ReactorType type) {
        super(Material.IRON);
        this.type = type;
        setRegistryName(NtmReactors.MODID, type.id);
        setUnlocalizedName(NtmReactors.MODID + "." + type.id);
        setCreativeTab(NtmReactors.TAB);
        setHardness(5.0F);
        setResistance(30.0F);
        setHarvestLevel("pickaxe", 1);
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return type.createTile();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player,
                                    EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (!world.isRemote) {
            player.openGui(NtmReactors.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof TileEntityReactorBase) {
            ((TileEntityReactorBase) te).dropContents();
        }
        super.breakBlock(world, pos, state);
    }
}

package me.pieking1215.immersive_telephones.common.block.switchboard;

import com.mojang.brigadier.LiteralMessage;
import me.pieking1215.immersive_telephones.common.tile_entity.TelephoneTileEntity;
import me.pieking1215.immersive_telephones.common.tile_entity.switchboard.SwitchboardTier1TileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;

public class SwitchboardTier1Block extends BaseSwitchboardBlock {
    public SwitchboardTier1Block(Properties properties) {
        super(properties);
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {

        if(!worldIn.isRemote){
            // server
            player.sendMessage(new StringTextComponent("clicked switchboard"), Util.DUMMY_UUID);
            for(int i = 0; i < 4; i++) {
                player.sendMessage(new StringTextComponent(i + ") " + worldIn.getBlockState(pos.offset(state.get(FACING), i + 1)).toString()), Util.DUMMY_UUID);
            }

            return ActionResultType.SUCCESS;
        }else{
            return ActionResultType.CONSUME;
        }

        //return worldIn.isRemote ? ActionResultType.FAIL : ActionResultType.PASS;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new SwitchboardTier1TileEntity();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!state.matchesBlock(newState.getBlock())){
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof SwitchboardTier1TileEntity){
                // on te broken
                worldIn.updateBlock(pos, this);
            }
            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.toRotation(state.get(FACING)));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(FACING);
    }
}

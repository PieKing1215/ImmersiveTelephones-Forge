package me.pieking1215.immersive_telephones.common.block.nid;

import mcp.MethodsReturnNonnullByDefault;
import me.pieking1215.immersive_telephones.common.block.peripheral.SpeakerTileEntity;
import me.pieking1215.immersive_telephones.common.block.switchboard.BaseSwitchboardBlock;
import me.pieking1215.immersive_telephones.common.block.switchboard.tier1.SwitchboardTier1TileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class NetworkInterfaceDeviceBlock extends BaseSwitchboardBlock<NetworkInterfaceDeviceTileEntity> {

    public NetworkInterfaceDeviceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(!(te instanceof NetworkInterfaceDeviceTileEntity)) return worldIn.isRemote ? ActionResultType.FAIL : ActionResultType.PASS;
        NetworkInterfaceDeviceTileEntity nid = (NetworkInterfaceDeviceTileEntity) te;

        ItemStack handStack = player.getHeldItem(handIn);
        if (handStack.getItem() instanceof NameTagItem && handStack.hasDisplayName()) {
            // name tag in hand

            if(!worldIn.isRemote()) {
                // server
                nid.setNumber(handStack.getDisplayName().getString());
                worldIn.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE);

                if(!player.isCreative()) handStack.shrink(1);

                return ActionResultType.CONSUME;
            }else{
                // client
                return ActionResultType.SUCCESS;
            }
        }

        return worldIn.isRemote ? ActionResultType.FAIL : ActionResultType.PASS;
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new NetworkInterfaceDeviceTileEntity();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!state.matchesBlock(newState.getBlock())){
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof NetworkInterfaceDeviceTileEntity){
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

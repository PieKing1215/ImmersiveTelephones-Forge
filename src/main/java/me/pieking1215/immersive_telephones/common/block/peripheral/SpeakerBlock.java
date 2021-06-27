package me.pieking1215.immersive_telephones.common.block.peripheral;

import me.pieking1215.immersive_telephones.common.block.peripheral.SpeakerTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.NameTagItem;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;

public class SpeakerBlock extends Block {

    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public SpeakerBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.getStateContainer().getBaseState()
                .with(ACTIVE, false));
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(ACTIVE);
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(!(te instanceof SpeakerTileEntity)) return worldIn.isRemote ? ActionResultType.FAIL : ActionResultType.PASS;
        SpeakerTileEntity speaker = (SpeakerTileEntity) te;

        ItemStack handStack = player.getHeldItem(handIn);
        if (handStack.getItem() instanceof NameTagItem && handStack.hasDisplayName()) {
            // name tag in hand

            if(!worldIn.isRemote()) {
                // server
                speaker.setNumber(handStack.getDisplayName().getString());
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
        return new SpeakerTileEntity();
    }
}

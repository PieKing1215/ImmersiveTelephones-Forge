package me.pieking1215.immersive_telephones.common.block;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.item.HandsetItem;
import me.pieking1215.immersive_telephones.common.tile_entity.TelephoneTileEntity;
import me.pieking1215.immersive_telephones.common.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.DyeColor;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.NameTagItem;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockDisplayReader;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public class TelephoneBlock extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty HANDSET = BooleanProperty.create("handset");

    @SuppressWarnings("SimplifyStreamApiCallChains")
    private static final VoxelShape SHAPE_WITH_HANDSET = Stream.of(
            Block.makeCuboidShape(4, 2, 0, 12, 14, 3)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR)).get();
    private static final Map<Direction, VoxelShape> FACING_SHAPES_WITH_HANDSET = Utils.generateRotatedShapes(SHAPE_WITH_HANDSET);

    private static final VoxelShape SHAPE_BASE = Stream.of(
            Block.makeCuboidShape(4, 2, 0, 12, 14, 1),
            Block.makeCuboidShape(4, 2, 1, 5, 14, 2),
            Block.makeCuboidShape(11, 2, 1, 12, 14, 2),
            Block.makeCuboidShape(5.25, 10.25, 1, 6.75, 11.75, 2),
            Block.makeCuboidShape(7.25, 10.25, 1, 8.75, 11.75, 2),
            Block.makeCuboidShape(9.25, 10.25, 1, 10.75, 11.75, 2),
            Block.makeCuboidShape(9.25, 8.25, 1, 10.75, 9.75, 2),
            Block.makeCuboidShape(7.25, 8.25, 1, 8.75, 9.75, 2),
            Block.makeCuboidShape(5.25, 8.25, 1, 6.75, 9.75, 2),
            Block.makeCuboidShape(5.25, 6.25, 1, 6.75, 7.75, 2),
            Block.makeCuboidShape(7.25, 6.25, 1, 8.75, 7.75, 2),
            Block.makeCuboidShape(9.25, 6.25, 1, 10.75, 7.75, 2),
            Block.makeCuboidShape(9.25, 4.25, 1, 10.75, 5.75, 2),
            Block.makeCuboidShape(7.25, 4.25, 1, 8.75, 5.75, 2),
            Block.makeCuboidShape(5.25, 4.25, 1, 6.75, 5.75, 2)
    ).reduce((v1, v2) -> VoxelShapes.combineAndSimplify(v1, v2, IBooleanFunction.OR)).get();
    private static final Map<Direction, VoxelShape> FACING_SHAPES_BASE = Utils.generateRotatedShapes(SHAPE_BASE);

    private static final List<VoxelShape> BUTTON_SHAPES = Arrays.asList(
            Block.makeCuboidShape(5.25, 10.25, 1, 6.75, 11.75, 2),
            Block.makeCuboidShape(7.25, 10.25, 1, 8.75, 11.75, 2),
            Block.makeCuboidShape(9.25, 10.25, 1, 10.75, 11.75, 2),
            Block.makeCuboidShape(5.25, 8.25, 1, 6.75, 9.75, 2),
            Block.makeCuboidShape(7.25, 8.25, 1, 8.75, 9.75, 2),
            Block.makeCuboidShape(9.25, 8.25, 1, 10.75, 9.75, 2),
            Block.makeCuboidShape(5.25, 6.25, 1, 6.75, 7.75, 2),
            Block.makeCuboidShape(7.25, 6.25, 1, 8.75, 7.75, 2),
            Block.makeCuboidShape(9.25, 6.25, 1, 10.75, 7.75, 2),
            Block.makeCuboidShape(5.25, 4.25, 1, 6.75, 5.75, 2),
            Block.makeCuboidShape(7.25, 4.25, 1, 8.75, 5.75, 2),
            Block.makeCuboidShape(9.25, 4.25, 1, 10.75, 5.75, 2)
    );


    // this data structure is backwards from what you might expect
    private static final List<Map<Direction, VoxelShape>> DIR_BUTTON_SHAPES = new ArrayList<>();

    TelephoneBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.getStateContainer().getBaseState()
                .with(FACING, Direction.NORTH)
                .with(HANDSET, true));

        for (VoxelShape buttonShape : BUTTON_SHAPES) {
            DIR_BUTTON_SHAPES.add(Utils.generateRotatedShapes(buttonShape));
        }
    }


    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        return state.get(HANDSET) ? FACING_SHAPES_WITH_HANDSET.get(state.get(FACING)) : FACING_SHAPES_BASE.get(state.get(FACING));
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return new TelephoneTileEntity();
    }

    @SuppressWarnings("deprecation")
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        TileEntity tileEntity = worldIn.getTileEntity(pos);
        if(tileEntity instanceof TelephoneTileEntity) {
            TelephoneTileEntity tel = (TelephoneTileEntity) tileEntity;

            ItemStack handStack = player.getHeldItem(handIn);
            if(handStack.getItem() instanceof DyeItem){
                tel.dyed(checkNotNull(DyeColor.getColor(handStack)));

                // TODO: for future reference, in 1.17 there is a sound for dyeing a sign

                if(!worldIn.isRemote){
                    if(!player.isCreative()){
                        handStack.shrink(1);
                    }
                }

                return worldIn.isRemote ? ActionResultType.SUCCESS : ActionResultType.CONSUME;

            }else if(handStack.getItem() == Items.WATER_BUCKET){
                tel.clearDye();

                return worldIn.isRemote ? ActionResultType.SUCCESS : ActionResultType.CONSUME;

            }else if (handStack.getItem() instanceof NameTagItem && handStack.hasDisplayName()) {
                // name tag in hand

                if(!worldIn.isRemote()) {
                    // server
                    tel.setNumber(handStack.getDisplayName().getString());
                    worldIn.notifyBlockUpdate(pos, state, state, Constants.BlockFlags.BLOCK_UPDATE);

                    if(!player.isCreative()) handStack.shrink(1);

                    return ActionResultType.CONSUME;
                }else{
                    // client
                    return ActionResultType.SUCCESS;
                }
            }else{
                // anything else in hand

                if(worldIn.isRemote()){
                    // client

                    ImmersiveTelephone.proxy.registerTelephoneAudioChannel(tel);

                    ItemStack it = player.getHeldItemMainhand();
                    if(it.isEmpty()) {
                        if (tel.isRinging()) {
                            // answer phone
                            return ActionResultType.SUCCESS;
                        } else {
                            if(tel.getHandsetEntity() == null) {
                                // pick up handset
                                return ActionResultType.SUCCESS;
                            }
                        }
                    }else{

                        if(player.isSneaking() && it.getItem() instanceof HandsetItem && tel.isTheHandset(it)){
                            // hang up
                            return ActionResultType.SUCCESS;
                        }

                    }

                }else{
                    // server

                    ItemStack it = player.getHeldItemMainhand();

                    if(it.isEmpty()) {
                        if (tel.isRinging()) {
                            if (tel.findConnectedPhones().stream().anyMatch(t -> t == tel.getWhoRings())) {
                                tel.answerPhone((ServerPlayerEntity) player);
                            }

                            return ActionResultType.CONSUME;
                        } else {
                            if(tel.getHandsetEntity() == null) {
                                tel.pickUpHandset((ServerPlayerEntity) player);

                                return ActionResultType.CONSUME;
                            }
                        }
                    }else{

                        if(player.isSneaking() && it.getItem() instanceof HandsetItem && tel.isTheHandset(it)){
                            // hang up
                            checkNotNull(tel.getWorld()).playSound(null, tel.getPos().getX() + 0.5, tel.getPos().getY() + 0.5, tel.getPos().getZ() + 0.5, player.isOnGround() ? SoundEvents.UI_BUTTON_CLICK : SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.BLOCKS, 1.0f, 1.0f);
                            player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                            tel.endCall();

                            return ActionResultType.CONSUME;
                        }

                    }

                }
            }

            if(!state.get(HANDSET)){
                float x = (float)(hit.getHitVec().getX() - Math.floor(hit.getHitVec().getX()));
                float y = (float)(hit.getHitVec().getY() - Math.floor(hit.getHitVec().getY()));
                float z = (float)(hit.getHitVec().getZ() - Math.floor(hit.getHitVec().getZ()));

                for(int i = 0; i < DIR_BUTTON_SHAPES.size(); i++){
                    if(DIR_BUTTON_SHAPES.get(i).get(state.get(FACING)).getBoundingBox().expand(0.01, 0.01, 0.01).contains(x, y, z)){
                        if(!worldIn.isRemote) tel.pressButton((ServerPlayerEntity) player, i);
                        return worldIn.isRemote ? ActionResultType.SUCCESS : ActionResultType.CONSUME;
                    }
                }
            }
        }

        return worldIn.isRemote ? ActionResultType.FAIL : ActionResultType.PASS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
        Direction direction = state.get(FACING).getOpposite();
        BlockPos blockpos = pos.offset(direction.getOpposite());
        BlockState blockstate = worldIn.getBlockState(blockpos);
        return blockstate.isSolidSide(worldIn, blockpos, direction);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        Direction dir = context.getFace();

        if(dir == Direction.UP || dir == Direction.DOWN) return null;

        return this.getDefaultState().with(FACING, dir.getOpposite());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!state.matchesBlock(newState.getBlock())){
            TileEntity tileEntity = worldIn.getTileEntity(pos);
            if(tileEntity instanceof TelephoneTileEntity){
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
        builder.add(HANDSET);
    }

    @SuppressWarnings("unused")
    public static int getColor(BlockState blockState, IBlockDisplayReader iBlockDisplayReader, BlockPos blockPos, int i) {
        if(i == 0){
            TileEntity tileEntity = iBlockDisplayReader.getTileEntity(blockPos);
            if(tileEntity instanceof TelephoneTileEntity) {
                TelephoneTileEntity tel = (TelephoneTileEntity) tileEntity;

                return tel.getColor();
            }
        }
        return 0xffffff;
    }
}

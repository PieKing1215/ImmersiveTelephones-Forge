package me.pieking1215.immersive_telephones.common.block.switchboard;

import me.pieking1215.immersive_telephones.common.tile_entity.ICallable;
import me.pieking1215.immersive_telephones.common.tile_entity.switchboard.BaseSwitchboardTileEntity;
import net.minecraft.block.Block;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import javax.annotation.Nonnull;
import java.util.Optional;

public class BaseSwitchboardBlock<TE extends BaseSwitchboardTileEntity> extends Block {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public BaseSwitchboardBlock(Properties properties) {
        super(properties);
        this.setDefaultState(this.getStateContainer().getBaseState()
                .with(FACING, Direction.NORTH));
    }

    @Nonnull
    public Optional<TE> getTileEntity(World world, BlockPos pos){
        if(!world.isBlockLoaded(pos)) return Optional.empty();

        TileEntity te = world.getTileEntity(pos);
        if(te == null) return Optional.empty();

        if(te.getClass().isInstance(te)) {
            //noinspection unchecked
            TE v = (TE) te;
            return Optional.of(v);
        }

        return Optional.empty();
    }

}

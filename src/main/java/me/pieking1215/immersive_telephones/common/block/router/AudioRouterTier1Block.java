package me.pieking1215.immersive_telephones.common.block.router;

import me.pieking1215.immersive_telephones.common.tile_entity.ICallable;
import me.pieking1215.immersive_telephones.common.tile_entity.IHasID;
import net.minecraft.block.Block;

public class AudioRouterTier1Block extends Block implements ICapacityHandler {
    public AudioRouterTier1Block(Properties properties) {
        super(properties);
    }

    @Override
    public <T extends IHasID> int getCapacity(Class<T> type) {
        if(type == ICallable.class) return 1;
        return 0;
    }
}

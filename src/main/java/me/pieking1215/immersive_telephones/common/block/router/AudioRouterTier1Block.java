package me.pieking1215.immersive_telephones.common.block.router;

import me.pieking1215.immersive_telephones.common.tile_entity.IAudioProvider;
import me.pieking1215.immersive_telephones.common.tile_entity.IAudioReceiver;
import net.minecraft.block.Block;

public class AudioRouterTier1Block extends Block implements ICapacityHandler {
    public AudioRouterTier1Block(Properties properties) {
        super(properties);
    }

    @Override
    public <T> int getCapacity(Class<T> type) {
        if(type == IAudioProvider.class) return 1;
        if(type == IAudioReceiver.class) return 1;
        return 0;
    }
}

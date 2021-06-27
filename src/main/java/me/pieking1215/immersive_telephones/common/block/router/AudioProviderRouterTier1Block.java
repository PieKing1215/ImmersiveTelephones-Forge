package me.pieking1215.immersive_telephones.common.block.router;

import me.pieking1215.immersive_telephones.common.block.IAudioProvider;
import net.minecraft.block.Block;

public class AudioProviderRouterTier1Block extends Block implements ICapacityHandler {
    public AudioProviderRouterTier1Block(Properties properties) {
        super(properties);
    }

    @Override
    public <T> int getCapacity(Class<T> type) {
        if(type == IAudioProvider.class) return 1;
        return 0;
    }
}

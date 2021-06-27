package me.pieking1215.immersive_telephones.common.block.router;

import me.pieking1215.immersive_telephones.common.block.IAudioReceiver;
import net.minecraft.block.Block;

public class AudioReceiverRouterTier1Block extends Block implements ICapacityHandler {
    public AudioReceiverRouterTier1Block(Properties properties) {
        super(properties);
    }

    @Override
    public <T> int getCapacity(Class<T> type) {
        if(type == IAudioReceiver.class) return 1;
        return 0;
    }
}

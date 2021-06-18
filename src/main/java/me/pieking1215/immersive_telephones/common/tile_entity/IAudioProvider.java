package me.pieking1215.immersive_telephones.common.tile_entity;

import net.minecraft.entity.player.PlayerEntity;

import java.util.Collection;

public interface IAudioProvider {

    boolean shouldProvideAudio(PlayerEntity source);

    Collection<IAudioReceiver> getRecievers(PlayerEntity source);

}

package me.pieking1215.immersive_telephones.common;

import me.pieking1215.immersive_telephones.common.tile_entity.IAudioPlayerHandler;
import me.pieking1215.immersive_telephones.common.tile_entity.TelephoneTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.InputEvent;

public class ServerProxy implements IProxy {
    @SuppressWarnings("unused")
    @Override
    public void openPhoneScreen(TelephoneTileEntity te) {
        // client only
    }

    @Override
    public boolean shouldCancelClick(InputEvent.ClickInputEvent ev) {
        return false;
    }

    @Override
    public void registerTelephoneAudioChannel(IAudioPlayerHandler te) {
        // client only
    }

    @Override
    public void loadConfig() {

    }

    @Override
    public PlayerEntity getLocalPlayer() {
        // client only
        return null;
    }
}

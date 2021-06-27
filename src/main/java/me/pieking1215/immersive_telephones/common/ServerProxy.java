package me.pieking1215.immersive_telephones.common;

import me.pieking1215.immersive_telephones.common.block.IAudioPlayerHandler;
import me.pieking1215.immersive_telephones.common.block.phone.tier1.TelephoneTier1TileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.client.event.InputEvent;

public class ServerProxy implements IProxy {
    @SuppressWarnings("unused")
    @Override
    public void openPhoneScreen(TelephoneTier1TileEntity te) {
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

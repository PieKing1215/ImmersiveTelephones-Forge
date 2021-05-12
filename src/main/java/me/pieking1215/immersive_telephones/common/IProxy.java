package me.pieking1215.immersive_telephones.common;

import me.pieking1215.immersive_telephones.common.tile_entity.TelephoneTileEntity;
import net.minecraftforge.client.event.InputEvent;

public interface IProxy {

    @SuppressWarnings("unused")
    void openPhoneScreen(TelephoneTileEntity te);

    boolean shouldCancelClick(InputEvent.ClickInputEvent ev);

    void registerTelephoneAudioChannel(TelephoneTileEntity te);

    void loadConfig();
}

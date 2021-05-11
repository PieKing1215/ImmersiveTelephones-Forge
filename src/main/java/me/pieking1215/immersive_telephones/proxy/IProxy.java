package me.pieking1215.immersive_telephones.proxy;

import me.pieking1215.immersive_telephones.tile_entity.TelephoneTileEntity;
import net.minecraftforge.client.event.InputEvent;

public interface IProxy {

    void openPhoneScreen(TelephoneTileEntity te);
    boolean shouldCancelClick(InputEvent.ClickInputEvent ev);
    void registerTelephoneAudioChannel(TelephoneTileEntity te);

}

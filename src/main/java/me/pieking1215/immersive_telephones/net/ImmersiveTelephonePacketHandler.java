package me.pieking1215.immersive_telephones.net;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class ImmersiveTelephonePacketHandler {

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ImmersiveTelephone.MOD_ID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    @SuppressWarnings("UnusedAssignment")
    public static void init(){
        int i = 0;
        INSTANCE.registerMessage(i++, StartCallPacket.class, StartCallPacket::encode, StartCallPacket::decode, StartCallPacket::handle);
    }

}

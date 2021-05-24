package me.pieking1215.immersive_telephones.common.network;

import me.pieking1215.immersive_telephones.common.Config;
import me.pieking1215.immersive_telephones.common.tile_entity.TelephoneTileEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncServerConfigPacket {

    public final double maxHandsetDistance;

    public SyncServerConfigPacket(double maxHandsetDistance) {
        this.maxHandsetDistance = maxHandsetDistance;
    }

    void encode(PacketBuffer buf){
        buf.writeDouble(maxHandsetDistance);
    }

    static SyncServerConfigPacket decode(PacketBuffer buf){
        return new SyncServerConfigPacket(buf.readDouble());
    }

    void handle(Supplier<NetworkEvent.Context> ctx){

        ctx.get().enqueueWork(() -> {
            if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                Config.makeDummyServerConfig(this);
            }

        });

        ctx.get().setPacketHandled(true);
    }

}

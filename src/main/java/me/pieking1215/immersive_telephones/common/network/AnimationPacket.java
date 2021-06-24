package me.pieking1215.immersive_telephones.common.network;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class AnimationPacket {

    private final BlockPos receiverPos;
    private final String channel;
    private final String animation;
    private final boolean loop;

    public AnimationPacket(BlockPos receiverPos, String channel, String animation, boolean loop) {
        this.receiverPos = receiverPos;
        this.channel = channel;
        this.animation = animation;
        this.loop = loop;
    }

    void encode(PacketBuffer buf){
        buf.writeBlockPos(receiverPos);
        buf.writeString(channel);
        buf.writeString(animation);
        buf.writeBoolean(loop);
    }

    static AnimationPacket decode(PacketBuffer buf){
        return new AnimationPacket(buf.readBlockPos(), buf.readString(32), buf.readString(64), buf.readBoolean()); // TODO: be defensive
    }

    void handle(Supplier<NetworkEvent.Context> ctx){

        ctx.get().enqueueWork(() -> {
            if(ctx.get().getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                PlayerEntity player = ImmersiveTelephone.proxy.getLocalPlayer();

                // TODO: these are only rough sanity checks
                //       (doesn't matter as much here since this is client side)

                //noinspection deprecation
                if(player != null && player.world.isBlockLoaded(receiverPos)){
                    TileEntity tileEntity = player.world.getTileEntity(receiverPos);
                    if(tileEntity instanceof IRecieveAnimationPacket){
                        IRecieveAnimationPacket anim = (IRecieveAnimationPacket)tileEntity;
                        anim.recieveAnimationPacket(channel, animation, loop);
                    }
                }
            }

        });

        ctx.get().setPacketHandled(true);
    }

}

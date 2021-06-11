package me.pieking1215.immersive_telephones.mixin.server;

import de.maxhenkel.voicechat.voice.common.MicPacket;
import de.maxhenkel.voicechat.voice.common.NetworkMessage;
import de.maxhenkel.voicechat.voice.common.SoundPacket;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Server;
import me.pieking1215.immersive_telephones.common.tile_entity.IAudioReceiver;
import me.pieking1215.immersive_telephones.common.tile_entity.TelephoneTileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Mixin(Server.class)
public abstract class MixinServer {

    @Shadow(remap = false)
    public abstract Map<UUID, ClientConnection> getConnections();

    @Inject(method = "processProximityPacket", at = @At("TAIL"), remap = false)
    private void injectProcessProximityPacket(PlayerEntity player, MicPacket packet, CallbackInfo ci){

        // welcome to the stream zone

        player.world.loadedTileEntityList.stream()
                .filter(t -> t instanceof TelephoneTileEntity)
                .map(t -> (TelephoneTileEntity)t)
                .filter(t -> player.equals(t.getHandsetEntity()))
                .findFirst().ifPresent(t -> {

                    // gather all players within 32 blocks of a phone that is in a call with this phone
                    float distance = 32;

                    t.getInCallWith().forEach(tel -> {
                        if(!(tel instanceof IAudioReceiver)) return;

                        IAudioReceiver receiver = (IAudioReceiver)tel;

                        if(receiver.getReceiverWorld() == null) return;

                        NetworkMessage msg = new NetworkMessage(new SoundPacket(receiver.getReceiverUUID(), packet.getData(), packet.getSequenceNumber()));
                        receiver.getReceiverWorld()
                                .getEntitiesWithinAABB(
                                        PlayerEntity.class,
                                        new AxisAlignedBB(
                                                receiver.getReceiverPos().getX() - distance,
                                                receiver.getReceiverPos().getY() - distance,
                                                receiver.getReceiverPos().getZ() - distance,
                                                receiver.getReceiverPos().getX() + distance,
                                                receiver.getReceiverPos().getY() + distance,
                                                receiver.getReceiverPos().getZ() + distance
                                        )
                                        , p -> !p.getUniqueID().equals(player.getUniqueID())
                                ).stream()
                                .map(pl -> getConnections().get(pl.getUniqueID()))
                                .filter(Objects::nonNull)
                                .forEach(c -> {
                                    try {
                                        c.send((Server)(Object)this, msg);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                });
                    });

        });
    }

}

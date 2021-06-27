package me.pieking1215.immersive_telephones.mixin.server;

import de.maxhenkel.voicechat.voice.common.MicPacket;
import de.maxhenkel.voicechat.voice.server.ClientConnection;
import de.maxhenkel.voicechat.voice.server.Server;
import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.block.IAudioProvider;
import me.pieking1215.immersive_telephones.common.block.IAudioReceiver;
import me.pieking1215.immersive_telephones.common.util.Utils;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.UUID;

@Mixin(Server.class)
public abstract class MixinServer {

    @Shadow(remap = false)
    public abstract Map<UUID, ClientConnection> getConnections();

    @Inject(method = "processProximityPacket", at = @At("TAIL"), remap = false)
    private void injectProcessProximityPacket(PlayerEntity player, MicPacket packet, CallbackInfo ci){
        // this needs to be run on the main thread
        //   since Utils::isFunctionalityFunctional uses World::getTileEntity which checks the thread
        ImmersiveTelephone.SCHEDULED.add(() -> {
            // welcome to the stream zone
            player.world.loadedTileEntityList.stream() // TODO: this is probably not performant
                    .filter(t -> t instanceof IAudioProvider)
                    .map(t -> (IAudioProvider) t)
                    .filter(t -> Utils.isFunctionalityFunctional(IAudioProvider.class, t) && t.shouldProvideAudio(player))
                    .findFirst().ifPresent(t -> { // TODO: this should be foreach
                t.getRecievers(player).forEach(receiver -> {
                    if (Utils.isFunctionalityFunctional(IAudioReceiver.class, receiver)) receiver.recieveAudio(packet);
                });
            });
        });
    }

}

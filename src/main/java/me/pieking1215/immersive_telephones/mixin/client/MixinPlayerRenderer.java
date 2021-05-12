package me.pieking1215.immersive_telephones.mixin.client;

import me.pieking1215.immersive_telephones.client.ICustomPoseHandler;
import me.pieking1215.immersive_telephones.common.item.HandsetItem;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.PlayerRenderer;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
class MixinPlayerRenderer {

    @Inject(method = "setModelVisibilities", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/client/renderer/entity/PlayerRenderer;func_241741_a_(Lnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/client/renderer/entity/model/BipedModel$ArmPose;"))
    private void injectChoosePose(AbstractClientPlayerEntity clientPlayer, CallbackInfo ci){
        if(clientPlayer.getHeldItem(Hand.MAIN_HAND).getItem() instanceof HandsetItem) {
            ((ICustomPoseHandler) ((PlayerRenderer) (Object) this).getEntityModel()).setHoldingPhone(true);
        }else{
            ((ICustomPoseHandler) ((PlayerRenderer) (Object) this).getEntityModel()).setHoldingPhone(false);
        }
    }
}

package me.pieking1215.immersive_telephones.mixin.client;

import me.pieking1215.immersive_telephones.client.ICustomPoseHandler;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BipedModel.class)
public class MixinBipedModel<T extends LivingEntity> implements ICustomPoseHandler {

    private boolean holdingPhone = false;

    @Inject(method = "func_241654_b_", at = @At("HEAD"), cancellable = true)
    private void injectPoseRightArm(T p_241654_1_, CallbackInfo ci){
        if(holdingPhone){
            BipedModel bm = (BipedModel)(Object)this;
            bm.bipedRightArm.rotateAngleY = bm.bipedHead.rotateAngleY / 2f;
            bm.bipedRightArm.rotateAngleZ = bm.bipedRightArm.rotateAngleZ - 0.25f - (bm.bipedHead.rotateAngleY / 4f);
            bm.bipedRightArm.rotateAngleX = bm.bipedRightArm.rotateAngleX * 0.5F - ((float)Math.PI * 0.75f) - (bm.bipedHead.rotateAngleY / 4f);
            ci.cancel();
        }
    }

    @Override
    public void setHoldingPhone(boolean holdingPhone) {
        this.holdingPhone = holdingPhone;
    }

    @Override
    public boolean getHoldingPhone() {
        return this.holdingPhone;
    }
}
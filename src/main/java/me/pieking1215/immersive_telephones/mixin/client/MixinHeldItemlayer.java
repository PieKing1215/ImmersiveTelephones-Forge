package me.pieking1215.immersive_telephones.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.pieking1215.immersive_telephones.common.item.HandsetItem;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.layers.HeldItemLayer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.HandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Deque;
import java.util.Iterator;

@Mixin(HeldItemLayer.class)
class MixinHeldItemlayer {

    @Inject(method = "func_229135_a_", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/FirstPersonRenderer;renderItemSide(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/client/renderer/model/ItemCameraTransforms$TransformType;ZLcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;I)V"))
    private void injectHeldItemRender(LivingEntity entity, ItemStack stack, ItemCameraTransforms.TransformType p_229135_3_, HandSide p_229135_4_, MatrixStack p_229135_5_, IRenderTypeBuffer p_229135_6_, int p_229135_7_, CallbackInfo ci){
        if(stack.getItem() instanceof HandsetItem){

            HandsetItem.findConnectedTE(stack, entity.world).ifPresent(tel -> {
                // final entry in the stack is the hand item matrix
                tel.clientHandItemMatrix4f = p_229135_5_.getLast().getMatrix().copy();

                // second entry in the stack is the camera matrix
                Deque<MatrixStack.Entry> msStack = ((MixinMatrixStackAccessor)p_229135_5_).getStack();
                Iterator<MatrixStack.Entry> stackIter = msStack.iterator();
                stackIter.next();
                tel.clientCameraMatrix4f = stackIter.next().getMatrix().copy();
            });

        }
    }

}

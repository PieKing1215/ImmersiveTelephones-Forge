package me.pieking1215.immersive_telephones.common.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3f;

public class HandsetEntityRenderer extends ItemRenderer {
    private final net.minecraft.client.renderer.ItemRenderer itemRenderer;

    public HandsetEntityRenderer(EntityRendererManager renderManagerIn, net.minecraft.client.renderer.ItemRenderer itemRendererIn) {
        super(renderManagerIn, itemRendererIn);
        itemRenderer = itemRendererIn;
        this.shadowSize = 0.3F;
        this.shadowOpaque = 0.5F;
    }

    @Override
    public void render(ItemEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        matrixStackIn.push();

        ItemStack itemstack = entityIn.getItem();
        IBakedModel ibakedmodel = this.itemRenderer.getItemModelWithOverrides(itemstack, entityIn.world, null);

        matrixStackIn.translate(0.0D, 0.25f, 0.0D);

        float f3 = entityIn.getItemHover(partialTicks);
        matrixStackIn.rotate(Vector3f.YP.rotation(f3));
        matrixStackIn.translate(0, f3 * 0.00001, 0); // helps with z fighting

        this.itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.GROUND, false, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, ibakedmodel);

        matrixStackIn.pop();

        // ItemRenderer usually calls super.render but all that does is draw the name plate so who cares
    }
}

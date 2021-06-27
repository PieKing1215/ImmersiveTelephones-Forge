package me.pieking1215.immersive_telephones.client.render.block_entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;

public class MultiTERenderer<T extends TileEntity/*, A extends TileEntityRenderer<? extends T>, B extends TileEntityRenderer<? extends T>*/> extends TileEntityRenderer<T> {

    final TileEntityRenderer<T>[] renderers;

    @SafeVarargs
    public MultiTERenderer(TileEntityRendererDispatcher rendererDispatcherIn,
                           TileEntityRenderer<T>... renderers) {
        super(rendererDispatcherIn);
        this.renderers = renderers;
    }

    @Override
    public void render(T tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        for (TileEntityRenderer<T> renderer : renderers) {
            renderer.render(tileEntityIn, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        }
    }
}

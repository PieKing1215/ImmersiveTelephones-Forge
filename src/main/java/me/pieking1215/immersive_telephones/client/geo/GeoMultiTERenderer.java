package me.pieking1215.immersive_telephones.client.geo;

import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoBlockRenderer;

public abstract class GeoMultiTERenderer<T extends TileEntity & IAnimatable> extends GeoBlockRenderer<T> {

    final TileEntityRenderer<T>[] renderers;

    @SafeVarargs
    public GeoMultiTERenderer(TileEntityRendererDispatcher rendererDispatcherIn, AnimatedGeoModel<T> modelProvider,
                              TileEntityRenderer<T>... renderers) {
        super(rendererDispatcherIn, modelProvider);
        this.renderers = renderers;
    }

    @Override
    public void render(TileEntity tile, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        super.render(tile, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);

        for (TileEntityRenderer<T> renderer : renderers) {
            //noinspection unchecked
            renderer.render((T) tile, partialTicks, matrixStackIn, bufferIn, combinedLightIn, combinedOverlayIn);
        }
    }
}

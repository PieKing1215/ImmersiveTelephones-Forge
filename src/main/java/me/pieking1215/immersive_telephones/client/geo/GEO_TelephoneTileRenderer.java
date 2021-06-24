package me.pieking1215.immersive_telephones.client.geo;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.pieking1215.immersive_telephones.client.tile_entity.HandsetPhoneTileEntityRenderer;
import me.pieking1215.immersive_telephones.client.tile_entity.ICallableTileEntityRenderer;
import me.pieking1215.immersive_telephones.common.tile_entity.TelephoneTileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import software.bernie.geckolib3.geo.render.built.GeoBone;

public class GEO_TelephoneTileRenderer extends GeoMultiTERenderer<TelephoneTileEntity> {
    public GEO_TelephoneTileRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn, new TelephoneModel(),
                new ICallableTileEntityRenderer<>(rendererDispatcherIn),
                new HandsetPhoneTileEntityRenderer<>(rendererDispatcherIn));
    }

    TelephoneTileEntity nowTile = null;

    @Override
    public void render(TelephoneTileEntity tile, float partialTicks, MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn) {
        stack.push();
        stack.translate(0, -0.01, 0);
        nowTile = tile;
        super.render(tile, partialTicks, stack, bufferIn, packedLightIn);
        nowTile = null;
        stack.pop();
    }

    @Override
    public void renderRecursively(GeoBone bone, MatrixStack stack, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
        if(bone.name.equals("handsetBone")){

            int color = 0xffffff;

            if(nowTile != null){
                color = nowTile.getColor();
            }

            int r = (color >> 16 & 255);
            int g = (color >> 8 & 255);
            int b = (color & 255);

            super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, r / 255f, g / 255f, b / 255f, alpha);
        }else {
            super.renderRecursively(bone, stack, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        }
    }
}

package me.pieking1215.immersive_telephones.client.render.block_entity.phone.tier1;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.pieking1215.immersive_telephones.client.render.block_entity.phone.HandsetPhoneRenderer;
import me.pieking1215.immersive_telephones.client.render.block_entity.ICallableRenderer;
import me.pieking1215.immersive_telephones.client.render.block_entity.GeoMultiTERenderer;
import me.pieking1215.immersive_telephones.common.block.phone.tier1.TelephoneTier1TileEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import software.bernie.geckolib3.geo.render.built.GeoBone;

public class TelephoneTier1Renderer extends GeoMultiTERenderer<TelephoneTier1TileEntity> {
    public TelephoneTier1Renderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn, new TelephoneTier1Model(),
                new ICallableRenderer<>(rendererDispatcherIn),
                new HandsetPhoneRenderer<>(rendererDispatcherIn));
    }

    TelephoneTier1TileEntity nowTile = null;

    @Override
    public void render(TelephoneTier1TileEntity tile, float partialTicks, MatrixStack stack, IRenderTypeBuffer bufferIn, int packedLightIn) {
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

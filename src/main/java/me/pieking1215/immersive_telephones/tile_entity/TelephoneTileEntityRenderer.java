package me.pieking1215.immersive_telephones.tile_entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.LightType;

public class TelephoneTileEntityRenderer extends TileEntityRenderer<TelephoneTileEntity> {
    public TelephoneTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(TelephoneTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Entity handset = tileEntityIn.getHandsetEntity();
        if(handset != null){
            renderLeash(tileEntityIn, partialTicks, matrixStackIn, bufferIn, handset);
        }
    }

    // based on MobRenderer::renderLeash
    private <E extends Entity> void renderLeash(TelephoneTileEntity te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, E leashHolder) {
        matrixStackIn.push();

        Vector3d vector3d = leashHolder.getLeashPosition(partialTicks);
        double d0 = (double)(MathHelper.lerp(partialTicks, 0/*entityLivingIn.renderYawOffset*/, 0/*entityLivingIn.prevRenderYawOffset*/) * ((float)Math.PI / 180F)) + (Math.PI / 2D);

        Vector3d vector3d1 = te.getCordConnectionPos();
        double d1 = Math.cos(d0) * vector3d1.z + Math.sin(d0) * vector3d1.x;
        double d2 = Math.sin(d0) * vector3d1.z - Math.cos(d0) * vector3d1.x;
        double d3 = Vector3d.copy(te.getPos()).getX() + d1;
        double d4 = Vector3d.copy(te.getPos()).getY() + vector3d1.y;
        double d5 = Vector3d.copy(te.getPos()).getZ() + d2;

        matrixStackIn.translate(d1, vector3d1.y, d2);

        float f = (float)(vector3d.x - d3);
        float f1 = (float)(vector3d.y - d4);
        float f2 = (float)(vector3d.z - d5);

        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getLeash());
        Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
        BlockPos blockpos = te.getPos();
        BlockPos blockpos1 = new BlockPos(leashHolder.getEyePosition(partialTicks));
        int i = te.getWorld().getLightFor(LightType.BLOCK, te.getPos());
        //int j = Minecraft.getInstance().getRenderManager().getRenderer(leashHolder).getBlockLight(leashHolder, blockpos1);
        int j = getBlockLight(leashHolder, blockpos1);
        int k = te.getWorld().getLightFor(LightType.SKY, blockpos);
        int l = te.getWorld().getLightFor(LightType.SKY, blockpos1);

        float f4 = MathHelper.fastInvSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;

        renderSide(ivertexbuilder, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6, partialTicks, leashHolder, te);
        renderSide(ivertexbuilder, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6, partialTicks, leashHolder, te);

        matrixStackIn.pop();
    }

    private int getBlockLight(Entity entityIn, BlockPos pos) {
        return entityIn.isBurning() ? 15 : entityIn.world.getLightFor(LightType.BLOCK, pos);
    }

    private static void renderSide(IVertexBuilder bufferIn, Matrix4f matrixIn, float p_229119_2_, float p_229119_3_, float p_229119_4_, int blockLight, int holderBlockLight, int skyLight, int holderSkyLight, float p_229119_9_, float p_229119_10_, float p_229119_11_, float p_229119_12_, float partialTicks, Entity leashHolder, TelephoneTileEntity te) {
        int nSegments = 80;

        for(int j = 0; j < nSegments; ++j) {
            float f = (float)j / (nSegments - 1);
            int k = (int)MathHelper.lerp(f, (float)blockLight, (float)holderBlockLight);
            int l = (int)MathHelper.lerp(f, (float)skyLight, (float)holderSkyLight);
            int i1 = LightTexture.packLight(k, l);
            addVertexPair(bufferIn, matrixIn, i1, p_229119_2_, p_229119_3_, p_229119_4_, p_229119_9_, p_229119_10_, nSegments, j, false, p_229119_11_, p_229119_12_, partialTicks, leashHolder, te);
            addVertexPair(bufferIn, matrixIn, i1, p_229119_2_, p_229119_3_, p_229119_4_, p_229119_9_, p_229119_10_, nSegments, j + 1, true, p_229119_11_, p_229119_12_, partialTicks, leashHolder, te);
        }

    }

    private static void addVertexPair(IVertexBuilder bufferIn, Matrix4f matrixIn, int packedLight, float deltaX, float deltaY, float deltaZ, float p_229120_6_, float p_229120_7_, int totalSegments, int segment, boolean p_229120_10_, float p_229120_11_, float p_229120_12_, float partialTicks, Entity leashHolder, TelephoneTileEntity te) {
        float r = 0.625F;
        float g = 0.6F;
        float b = 0.55F;

        float telR = (te.getColor() >> 16 & 255) / 255f;
        float telG = (te.getColor() >> 8 & 255) / 255f;
        float telB = (te.getColor() & 255) / 255f;

        r = 0.8f * r + 0.2f * telR;
        g = 0.8f * g + 0.2f * telG;
        b = 0.8f * b + 0.2f * telB;

        if (segment % 2 == 0) {
            r *= 0.85F;
            g *= 0.85F;
            b *= 0.85F;
        }

        float thru = (float)segment / (float)totalSegments;

        Vector3d forward = new Vector3d(deltaX, deltaY, deltaZ).normalize();
        Vector3d horiz = forward.crossProduct(new Vector3d(0, 1, 0));
        Vector3d up = forward.crossProduct(horiz);

        Vector3d spiralOffsetRaw = new Vector3d(Math.sin(thru * totalSegments), Math.cos(thru * totalSegments), 0).scale(0.05f);

        Vector3d spiralOffsetTransformed = horiz.scale(spiralOffsetRaw.x).add(up.scale(spiralOffsetRaw.y)).add(forward.scale(spiralOffsetRaw.z));

        float thruX = deltaX * thru;

//        float thruForY = thru;
//        if(thru > 0.85){
//            float thruThru = (thru - 0.85f) / 0.15f;
//            thruForY = thru + (1f - thru) * thruThru;
//        }

//        float thruY = deltaY > 0.0F ?
//                (2f * deltaY * thruForY * thruForY - deltaY * thruForY) :
//                deltaY - 2f * deltaY * (1f - thruForY) * (1f - thruForY) + deltaY * (1f - thruForY);

        float thruY = deltaY > 0f ?
                deltaY * thru * thru :
                deltaY - deltaY * (1.0F - thru) * (1.0F - thru);

        float thruZ = deltaZ * thru;

        thruX += spiralOffsetTransformed.getX();
        thruY += spiralOffsetTransformed.getY();
        thruZ += spiralOffsetTransformed.getZ();

        if (!p_229120_10_) {
            bufferIn.pos(matrixIn, thruX + p_229120_11_, thruY + p_229120_6_ - p_229120_7_, thruZ - p_229120_12_).color(r, g, b, 1.0F).lightmap(packedLight).endVertex();
        }

        bufferIn.pos(matrixIn, thruX - p_229120_11_, thruY + p_229120_7_, thruZ + p_229120_12_).color(r, g, b, 1.0F).lightmap(packedLight).endVertex();
        if (p_229120_10_) {
            bufferIn.pos(matrixIn, thruX + p_229120_11_, thruY + p_229120_6_ - p_229120_7_, thruZ - p_229120_12_).color(r, g, b, 1.0F).lightmap(packedLight).endVertex();
        }

    }

}

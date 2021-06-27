package me.pieking1215.immersive_telephones.client.render.block_entity.phone;

import com.google.common.base.Preconditions;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.pieking1215.immersive_telephones.common.Config;
import me.pieking1215.immersive_telephones.common.block.phone.HandsetPhoneTileEntity;
import me.pieking1215.immersive_telephones.common.block.phone.tier1.TelephoneTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.settings.PointOfView;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.HandSide;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import net.minecraft.world.LightType;

public class HandsetPhoneRenderer<T extends HandsetPhoneTileEntity> extends TileEntityRenderer<T> {
    public HandsetPhoneRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(HandsetPhoneTileEntity tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        Entity handset = tileEntityIn.getHandsetEntity();
        if(handset != null){
            renderCord(tileEntityIn, partialTicks, matrixStackIn, bufferIn, handset);
        }

        // this does nothing since inCallWith is not synced to clients
//        tileEntityIn.getInCallWith().forEach(w -> {
//            if(w instanceof IAudioReceiver) {
//                renderLine(partialTicks, matrixStackIn, bufferIn, Vector3d.copyCentered(tileEntityIn.getReceiverPos()), Vector3d.copyCentered(((IAudioReceiver) w).getReceiverPos()));
//            }
//        });

    }

    // based on MobRenderer::renderLeash
    private void renderLine(float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, Vector3d pos1, Vector3d pos2) {
        matrixStackIn.push();

        double d0 = (double)(MathHelper.lerp(partialTicks, 0/*entityLivingIn.renderYawOffset*/, 0/*entityLivingIn.prevRenderYawOffset*/) * ((float)Math.PI / 180F)) + (Math.PI / 2D);

        double d1 = Math.cos(d0) * pos1.z + Math.sin(d0) * pos1.x;
        double d2 = Math.sin(d0) * pos1.z - Math.cos(d0) * pos1.x;
        double d3 = pos1.getX() + d1;
        double d4 = pos1.getY() + pos1.y;
        double d5 = pos1.getZ() + d2;

        matrixStackIn.translate(d1, pos1.y, d2);

        float f = (float)(pos2.x - d3);
        float f1 = (float)(pos2.y - d4);
        float f2 = (float)(pos2.z - d5);

        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getLeash());
        Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
        BlockPos blockpos = new BlockPos(pos1);
        BlockPos blockpos1 = new BlockPos(pos2);
        int i = 15;
        //int j = Minecraft.getInstance().getRenderManager().getRenderer(leashHolder).getBlockLight(leashHolder, blockpos1);
        int j = 15;
        int k = 15;
        int l = 15;

        float f4 = MathHelper.fastInvSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;

        renderSide2(ivertexbuilder, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6, partialTicks);
        renderSide2(ivertexbuilder, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6, partialTicks);

        matrixStackIn.pop();
    }

    private static void renderSide2(IVertexBuilder bufferIn, Matrix4f matrixIn, float p_229119_2_, float p_229119_3_, float p_229119_4_, int blockLight, int holderBlockLight, int skyLight, int holderSkyLight, @SuppressWarnings("SameParameterValue") float p_229119_9_, float p_229119_10_, float p_229119_11_, float p_229119_12_, float partialTicks) {
        int nSegments = 24;

        for(int j = 0; j < nSegments; ++j) {
            float f = (float)j / (nSegments - 1);
            int k = (int)MathHelper.lerp(f, (float)blockLight, (float)holderBlockLight);
            int l = (int)MathHelper.lerp(f, (float)skyLight, (float)holderSkyLight);
            int i1 = LightTexture.packLight(k, l);
            addVertexPair2(bufferIn, matrixIn, i1, p_229119_2_, p_229119_3_, p_229119_4_, p_229119_9_, p_229119_10_, nSegments, j, false, p_229119_11_, p_229119_12_, partialTicks);
            addVertexPair2(bufferIn, matrixIn, i1, p_229119_2_, p_229119_3_, p_229119_4_, p_229119_9_, p_229119_10_, nSegments, j + 1, true, p_229119_11_, p_229119_12_, partialTicks);
        }

    }

    private static void addVertexPair2(IVertexBuilder bufferIn, Matrix4f matrixIn, int packedLight, float deltaX, float deltaY, float deltaZ, float p_229120_6_, float p_229120_7_, int totalSegments, int segment, boolean p_229120_10_, float p_229120_11_, float p_229120_12_, float partialTicks) {
        float r = 0.3F;
        float g = 0.8F;
        float b = 0F;

        float thru = (float)segment / (float)totalSegments;

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

        if(Config.CLIENT.fancyCordRendering.get()) {
            Vector3d forward = new Vector3d(deltaX, deltaY, deltaZ).normalize();
            Vector3d horiz = forward.crossProduct(new Vector3d(0, 1, 0));
            Vector3d up = forward.crossProduct(horiz);

            Vector3d spiralOffsetRaw = new Vector3d(Math.sin(thru * totalSegments), Math.cos(thru * totalSegments), 0).scale(0.05f);

            Vector3d spiralOffsetTransformed = horiz.scale(spiralOffsetRaw.x).add(up.scale(spiralOffsetRaw.y)).add(forward.scale(spiralOffsetRaw.z));


            thruX += spiralOffsetTransformed.getX();
            thruY += spiralOffsetTransformed.getY();
            thruZ += spiralOffsetTransformed.getZ();
        }

        if (!p_229120_10_) {
            bufferIn.pos(matrixIn, thruX + p_229120_11_, thruY + p_229120_6_ - p_229120_7_, thruZ - p_229120_12_).color(r, g, b, 1.0F).lightmap(packedLight).endVertex();
        }

        bufferIn.pos(matrixIn, thruX - p_229120_11_, thruY + p_229120_7_, thruZ + p_229120_12_).color(r, g, b, 1.0F).lightmap(packedLight).endVertex();
        if (p_229120_10_) {
            bufferIn.pos(matrixIn, thruX + p_229120_11_, thruY + p_229120_6_ - p_229120_7_, thruZ - p_229120_12_).color(r, g, b, 1.0F).lightmap(packedLight).endVertex();
        }

    }

    // based on MobRenderer::renderLeash
    private <E extends Entity> void renderCord(HandsetPhoneTileEntity te, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, E holder) {
        matrixStackIn.push();

        Vector3d vector3d = holder.getLeashPosition(partialTicks);
        if(holder instanceof PlayerEntity) vector3d = getHoldingPos(te, (PlayerEntity)holder, partialTicks);
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
        BlockPos blockpos1 = new BlockPos(holder.getEyePosition(partialTicks));
        int i = Preconditions.checkNotNull(te.getWorld()).getLightFor(LightType.BLOCK, te.getPos());
        //int j = Minecraft.getInstance().getRenderManager().getRenderer(leashHolder).getBlockLight(leashHolder, blockpos1);
        int j = getBlockLight(holder, blockpos1);
        int k = te.getWorld().getLightFor(LightType.SKY, blockpos);
        int l = te.getWorld().getLightFor(LightType.SKY, blockpos1);

        float f4 = MathHelper.fastInvSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;

        renderSide(ivertexbuilder, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6, partialTicks, holder, te);
        renderSide(ivertexbuilder, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6, partialTicks, holder, te);

        matrixStackIn.pop();
    }

    private Vector3d getHoldingPos(HandsetPhoneTileEntity te, PlayerEntity holder, float partialTicks) {
        HandSide holdingHand = te.getHoldingHand(holder);

        if(!Config.CLIENT.accurateCordAttachment.get() || (Minecraft.getInstance().player == holder && Minecraft.getInstance().gameSettings.getPointOfView() == PointOfView.FIRST_PERSON)){

            if(holdingHand == holder.getPrimaryHand()){
                return holder.getLeashPosition(partialTicks);
            }else{
                // getLeashPosition depends on the primary hand
                // so we can just flip it for this one call to get it for the other hand
                holder.setPrimaryHand(holder.getPrimaryHand().opposite());
                Vector3d pos = holder.getLeashPosition(partialTicks);
                holder.setPrimaryHand(holder.getPrimaryHand().opposite());
                return pos;
            }
        }

        Vector3f pos = new Vector3f(0, 0, 0);

        if(te.clientHandItemMatrix4f instanceof Matrix4f){
            Preconditions.checkState(te.clientCameraMatrix4f instanceof Matrix4f);

            Matrix4f itemMatrix = (Matrix4f) te.clientHandItemMatrix4f;
            Matrix4f cameraMatrix = (Matrix4f) te.clientCameraMatrix4f;

            Preconditions.checkNotNull(itemMatrix);
            Preconditions.checkNotNull(cameraMatrix);

            cameraMatrix.invert();

            Vector4f v4 = new Vector4f(
                    pos.getX() + (holdingHand == HandSide.LEFT ? 0.1f : -0.1f),
                    pos.getY() - 0.325f,
                    pos.getZ() + 0.25f, 1.0f);

            //itemMatrix.mul(Matrix4f.makeTranslate(-0.5f, -0.5f, -0.5f));
            v4.transform(itemMatrix);
            v4.transform(cameraMatrix);

            return holder.getEyePosition(partialTicks).subtract(0, holder.getEyeHeight(), 0)
                    .add(new Vector3d(v4.getX(), v4.getY(), v4.getZ()));
        }

        return new Vector3d(pos.getX(), pos.getY(), pos.getZ());
    }

    private int getBlockLight(Entity entityIn, BlockPos pos) {
        return entityIn.isBurning() ? 15 : entityIn.world.getLightFor(LightType.BLOCK, pos);
    }

    @SuppressWarnings("SameParameterValue")
    private static void renderSide(IVertexBuilder bufferIn, Matrix4f matrixIn, float p_229119_2_, float p_229119_3_, float p_229119_4_, int blockLight, int holderBlockLight, int skyLight, int holderSkyLight, float p_229119_9_, float p_229119_10_, float p_229119_11_, float p_229119_12_, float partialTicks, Entity holder, HandsetPhoneTileEntity te) {
        int nSegments = Config.CLIENT.fancyCordRendering.get() ? 80 : 24;

        for(int j = 0; j < nSegments; ++j) {
            float f = (float)j / (nSegments - 1);
            int k = (int)MathHelper.lerp(f, (float)blockLight, (float)holderBlockLight);
            int l = (int)MathHelper.lerp(f, (float)skyLight, (float)holderSkyLight);
            int i1 = LightTexture.packLight(k, l);
            addVertexPair(bufferIn, matrixIn, i1, p_229119_2_, p_229119_3_, p_229119_4_, p_229119_9_, p_229119_10_, nSegments, j, false, p_229119_11_, p_229119_12_, partialTicks, holder, te);
            addVertexPair(bufferIn, matrixIn, i1, p_229119_2_, p_229119_3_, p_229119_4_, p_229119_9_, p_229119_10_, nSegments, j + 1, true, p_229119_11_, p_229119_12_, partialTicks, holder, te);
        }

    }

    @SuppressWarnings("unused")
    private static void addVertexPair(IVertexBuilder bufferIn, Matrix4f matrixIn, int packedLight, float deltaX, float deltaY, float deltaZ, float p_229120_6_, float p_229120_7_, int totalSegments, int segment, boolean p_229120_10_, float p_229120_11_, float p_229120_12_, float partialTicks, Entity holder, HandsetPhoneTileEntity te) {
        float r = 0.625F;
        float g = 0.6F;
        float b = 0.55F;

        if(te instanceof TelephoneTileEntity) {
            float telR = (((TelephoneTileEntity)te).getColor() >> 16 & 255) / 255f;
            float telG = (((TelephoneTileEntity)te).getColor() >> 8 & 255) / 255f;
            float telB = (((TelephoneTileEntity)te).getColor() & 255) / 255f;

            r = 0.8f * r + 0.2f * telR;
            g = 0.8f * g + 0.2f * telG;
            b = 0.8f * b + 0.2f * telB;
        }

        if (segment % 2 == 0) {
            r *= 0.85F;
            g *= 0.85F;
            b *= 0.85F;
        }

        float thru = (float)segment / (float)totalSegments;

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

        if(Config.CLIENT.fancyCordRendering.get()) {
            Vector3d forward = new Vector3d(deltaX, deltaY, deltaZ).normalize();
            Vector3d horiz = forward.crossProduct(new Vector3d(0, 1, 0));
            Vector3d up = forward.crossProduct(horiz);

            Vector3d spiralOffsetRaw = new Vector3d(Math.sin(thru * totalSegments), Math.cos(thru * totalSegments), 0).scale(0.05f);

            Vector3d spiralOffsetTransformed = horiz.scale(spiralOffsetRaw.x).add(up.scale(spiralOffsetRaw.y)).add(forward.scale(spiralOffsetRaw.z));


            thruX += spiralOffsetTransformed.getX();
            thruY += spiralOffsetTransformed.getY();
            thruZ += spiralOffsetTransformed.getZ();
        }

        if (!p_229120_10_) {
            bufferIn.pos(matrixIn, thruX + p_229120_11_, thruY + p_229120_6_ - p_229120_7_, thruZ - p_229120_12_).color(r, g, b, 1.0F).lightmap(packedLight).endVertex();
        }

        bufferIn.pos(matrixIn, thruX - p_229120_11_, thruY + p_229120_7_, thruZ + p_229120_12_).color(r, g, b, 1.0F).lightmap(packedLight).endVertex();
        if (p_229120_10_) {
            bufferIn.pos(matrixIn, thruX + p_229120_11_, thruY + p_229120_6_ - p_229120_7_, thruZ - p_229120_12_).color(r, g, b, 1.0F).lightmap(packedLight).endVertex();
        }

    }

}

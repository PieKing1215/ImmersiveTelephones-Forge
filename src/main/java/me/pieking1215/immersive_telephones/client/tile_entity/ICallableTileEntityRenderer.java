package me.pieking1215.immersive_telephones.client.tile_entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.pieking1215.immersive_telephones.common.tile_entity.ICallable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.tileentity.TileEntityRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class ICallableTileEntityRenderer<T extends TileEntity & ICallable> extends TileEntityRenderer<T> {
    public ICallableTileEntityRenderer(TileEntityRendererDispatcher rendererDispatcherIn) {
        super(rendererDispatcherIn);
    }

    @Override
    public void render(T tileEntityIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        renderName(tileEntityIn, new StringTextComponent(tileEntityIn.getID()), matrixStackIn, bufferIn, combinedLightIn);
    }

    // based on EntityRenderer::renderName
    protected void renderName(T tileEntityIn, ITextComponent displayNameIn, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        double sqDist = Minecraft.getInstance().getRenderManager().info.getProjectedView().squareDistanceTo(Vector3d.copyCentered(tileEntityIn.getPos()));
        if (net.minecraftforge.client.ForgeHooksClient.isNameplateInRenderDistance(null, sqDist)) {
            matrixStackIn.push();

            //noinspection ConstantConditions,deprecation
            AxisAlignedBB aabb = tileEntityIn.getBlockState().getBlock().getShape(tileEntityIn.getBlockState(),
                    tileEntityIn.getWorld(),
                    tileEntityIn.getPos(),
                    ISelectionContext.dummy()).getBoundingBox();
            Vector3d topCenter = new Vector3d(aabb.getCenter().x, aabb.maxY, aabb.getCenter().z);

            matrixStackIn.translate(topCenter.getX(), topCenter.getY() + 0.4f, topCenter.getZ());
            matrixStackIn.rotate(Minecraft.getInstance().getRenderManager().getCameraOrientation());
            matrixStackIn.scale(-0.025F, -0.025F, 0.025F);

            float bgOpacity = Minecraft.getInstance().gameSettings.getTextBackgroundOpacity(0.25F);
            int bgCol = (int)(bgOpacity * 255.0F) << 24;
            float textWidth = -Minecraft.getInstance().fontRenderer.getStringPropertyWidth(displayNameIn);

            //drawEntityText
            Minecraft.getInstance().fontRenderer.func_243247_a(displayNameIn, textWidth/2, 0, 0xffffffff, false, matrixStackIn.getLast().getMatrix(), bufferIn, false, bgCol, packedLightIn);

            matrixStackIn.pop();
        }
    }
}

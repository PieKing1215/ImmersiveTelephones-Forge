package me.pieking1215.immersive_telephones.client.render.block_entity.phone.tier1;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import me.pieking1215.immersive_telephones.common.block.phone.tier1.TelephoneTier1Block;
import me.pieking1215.immersive_telephones.common.block.phone.tier1.TelephoneTier1TileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.builder.Animation;
import software.bernie.geckolib3.core.keyframe.KeyFrame;
import software.bernie.geckolib3.core.keyframe.VectorKeyFrameList;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.shadowed.eliotlash.mclib.math.IValue;

public class TelephoneTier1Model extends AnimatedGeoModel<TelephoneTier1TileEntity> {
    @Override
    public ResourceLocation getModelLocation(TelephoneTier1TileEntity telephoneBlock) {
        return new ResourceLocation(ImmersiveTelephone.MOD_ID, "geo/telephone_block.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(TelephoneTier1TileEntity telephoneBlock) {
        return new ResourceLocation(ImmersiveTelephone.MOD_ID, "textures/blocks/telephone_block.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(TelephoneTier1TileEntity telephoneBlock) {
        return new ResourceLocation(ImmersiveTelephone.MOD_ID, "animations/telephone_block.animation.json");
    }

    @Override
    public Animation getAnimation(String name, IAnimatable animatable) {
        Animation animation = super.getAnimation(name, animatable);

        if(animatable instanceof TelephoneTier1TileEntity){
            TelephoneTier1TileEntity te = (TelephoneTier1TileEntity) animatable;

            if (te.getHandsetEntity() != null) {
                Vector3d diff = te.getHandsetEntity().getEyePosition(Minecraft.getInstance().getRenderPartialTicks())
                        .subtract(Vector3d.copyCentered(te.getPos())).scale(16)
                        .add(new Vector3d(0, 4, 0));

                Vector3d rotatedDiff = Vector3d.copy(te.getBlockState().get(TelephoneTier1Block.FACING).getDirectionVec()).scale(-diff.z)
                        .add(Vector3d.copy(te.getBlockState().get(TelephoneTier1Block.FACING).rotateYCCW().getDirectionVec()).scale(diff.x))
                        .add(new Vector3d(0, diff.y, 0));

                if(name.equals("animation.telephone_block.hide_handset")) {
                    VectorKeyFrameList<KeyFrame<IValue>> kf = animation.boneAnimations.get(animation.boneAnimations.size() - 1).positionKeyFrames;
                    kf.xKeyFrames.get(kf.xKeyFrames.size() - 1).setEndValue(() -> rotatedDiff.x);
                    kf.yKeyFrames.get(kf.yKeyFrames.size() - 1).setEndValue(() -> rotatedDiff.y);
                    kf.zKeyFrames.get(kf.zKeyFrames.size() - 1).setEndValue(() -> rotatedDiff.z);
                }else if(name.startsWith("animation.telephone_block.place_handset")) {
                    VectorKeyFrameList<KeyFrame<IValue>> kf = animation.boneAnimations.get(animation.boneAnimations.size() - 1).positionKeyFrames;
                    kf.xKeyFrames.get(1).setStartValue(() -> rotatedDiff.x);
                    kf.yKeyFrames.get(1).setStartValue(() -> rotatedDiff.y);
                    kf.zKeyFrames.get(1).setStartValue(() -> rotatedDiff.z);
                }
            }
        }
        return animation;
    }
}

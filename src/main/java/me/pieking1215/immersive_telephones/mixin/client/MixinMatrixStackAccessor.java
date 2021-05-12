package me.pieking1215.immersive_telephones.mixin.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Deque;

@SuppressWarnings("WeakerAccess")
@Mixin(MatrixStack.class)
public interface MixinMatrixStackAccessor {

    @Accessor("stack")
    Deque<MatrixStack.Entry> getStack();

}

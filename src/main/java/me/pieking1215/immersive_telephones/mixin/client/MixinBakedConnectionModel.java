package me.pieking1215.immersive_telephones.mixin.client;

import blusunrize.immersiveengineering.client.models.connection.BakedConnectionModel;
import me.pieking1215.immersive_telephones.client.geo.IBakedConnectionModelForceDisplay;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.BakedQuad;
import net.minecraftforge.client.MinecraftForgeClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(BakedConnectionModel.class)
public class MixinBakedConnectionModel implements IBakedConnectionModelForceDisplay {

    private static boolean _imm_tele_force_display = false;

    @Redirect(method = "getQuads", remap = false, at = @At(value = "INVOKE", target = "Lblusunrize/immersiveengineering/client/models/connection/BakedConnectionModel$SpecificConnectionModel;getQuads(Lnet/minecraft/client/renderer/RenderType;)Ljava/util/List;"))
    private List<BakedQuad> qq(BakedConnectionModel.SpecificConnectionModel specificConnectionModel, RenderType layer){
        return specificConnectionModel.getQuads(_imm_tele_force_display ? RenderType.getSolid() : layer);
    }

    //    @ModifyVariable(method = "getQuads", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraftforge/client/MinecraftForgeClient;getRenderLayer()Lnet/minecraft/client/renderer/RenderType;"))
//    private RenderType rl(RenderType orig){
//        if(_imm_tele_force_display) return RenderType.getSolid();
//        return orig;
//    }

//    @Redirect(method = "getQuads", remap = false, at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraftforge/client/MinecraftForgeClient;getRenderLayer()Lnet/minecraft/client/renderer/RenderType;", remap = false))
//    private static RenderType getRenderLayer(){
//        if(_imm_tele_force_display) return RenderType.getSolid();
//        return MinecraftForgeClient.getRenderLayer();
//    }

    @Override
    public void set(boolean val) {
        _imm_tele_force_display = val;
    }
}

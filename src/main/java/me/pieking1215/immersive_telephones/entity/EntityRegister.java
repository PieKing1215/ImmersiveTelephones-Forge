package me.pieking1215.immersive_telephones.entity;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EntityRegister {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITIES, ImmersiveTelephone.MOD_ID);

    public static final RegistryObject<EntityType<HandsetEntity>> HANDSET = ENTITY_TYPES.register("handset",
            () -> EntityType.Builder.create(EntityRegister::createHandset, EntityClassification.MISC)
                    .size(12f / 16f, 0.3F)
                    .build(new ResourceLocation(ImmersiveTelephone.MOD_ID, "handset").toString()));

    // need to do this weird extra method because putting this in the lambda breaks the generics
    private static HandsetEntity createHandset(EntityType<HandsetEntity> t, World w) {
        return new HandsetEntity(t, w);
    }
}

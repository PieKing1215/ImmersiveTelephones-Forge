package me.pieking1215.immersive_telephones.data.client;

import me.pieking1215.immersive_telephones.ImmersiveTelephone;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, ImmersiveTelephone.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent("telephone", modLoc("block/telephone_with_handset"));
        withExistingParent("switchboard_t1", modLoc("block/switchboard_t1"));
    }
}

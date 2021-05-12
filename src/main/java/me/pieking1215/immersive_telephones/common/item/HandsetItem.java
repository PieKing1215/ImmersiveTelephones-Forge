package me.pieking1215.immersive_telephones.common.item;

import com.google.common.base.Preconditions;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;

public class HandsetItem extends Item {
    HandsetItem(Properties properties) {
        super(properties);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
        BlockPos connected = getConnectedPosition(stack);

        if (connected != null) {
            tooltip.add((new StringTextComponent(connected.toString())).mergeStyle(TextFormatting.GOLD));
        }
    }

    public static BlockPos getConnectedPosition(ItemStack stack){
        if(!(stack.getItem() instanceof HandsetItem)) return null;

        if (stack.hasTag()) {
            CompoundNBT nbt = stack.getTag();

            Preconditions.checkState(nbt != null);

            return new BlockPos(
                    nbt.getInt("connected_x"),
                    nbt.getInt("connected_y"),
                    nbt.getInt("connected_z")
            );
        }

        return null;
    }

    public static void setColor(ItemStack stack, int color){
        stack.getOrCreateTag().putInt("color", color);
    }

    public static int getItemColor(ItemStack itemStack, int i) {
        if (i == 0) {
            CompoundNBT nbt = itemStack.getOrCreateTag();
            if(nbt.contains("color")) return nbt.getInt("color");
        }

        return 0xffffff;
    }
}

package me.pieking1215.immersive_telephones.common.item;

import com.google.common.base.Preconditions;
import me.pieking1215.immersive_telephones.common.block.phone.tier1.TelephoneTileEntity;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

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

    public static void write(ItemStack stack, BlockPos pos, int color){
        CompoundNBT nbt = new CompoundNBT();

        Preconditions.checkNotNull(stack);
        Preconditions.checkNotNull(pos);

        nbt.putInt("connected_x", pos.getX());
        nbt.putInt("connected_y", pos.getY());
        nbt.putInt("connected_z", pos.getZ());

        nbt.putInt("color", color);

        stack.setTag(nbt);
    }

    @SuppressWarnings("WeakerAccess")
    @Nullable
    public static BlockPos getConnectedPosition(ItemStack stack){
        if(!(stack.getItem() instanceof HandsetItem)) return null;

        if (stack.hasTag()) {
            CompoundNBT nbt = stack.getTag();

            Preconditions.checkNotNull(nbt);

            return new BlockPos(
                    nbt.getInt("connected_x"),
                    nbt.getInt("connected_y"),
                    nbt.getInt("connected_z")
            );
        }

        return null;
    }

    public static Optional<TelephoneTileEntity> findConnectedTE(ItemStack stack, World world){
        BlockPos telPos = HandsetItem.getConnectedPosition(stack);

        if(telPos == null) {
            // invalid item
            return Optional.empty();
        }

        Preconditions.checkNotNull(world);

        //noinspection deprecation
        if(!world.isBlockLoaded(telPos)){
            // the position this is supposed to be connected to is not chunk loaded
            return Optional.empty();
        }

        TileEntity te = world.getTileEntity(telPos);
        if(!(te instanceof TelephoneTileEntity)){
            // there is no telephone here
            return Optional.empty();
        }

        return Optional.of((TelephoneTileEntity) te);
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

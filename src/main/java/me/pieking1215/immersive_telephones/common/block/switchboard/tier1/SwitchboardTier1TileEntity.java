package me.pieking1215.immersive_telephones.common.block.switchboard.tier1;

import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorTileEntity;
import me.pieking1215.immersive_telephones.common.block.IAudioProvider;
import me.pieking1215.immersive_telephones.common.block.IAudioReceiver;
import me.pieking1215.immersive_telephones.common.block.ICallable;
import me.pieking1215.immersive_telephones.common.block.TileEntityRegister;
import me.pieking1215.immersive_telephones.common.block.switchboard.BaseSwitchboardTileEntity;
import me.pieking1215.immersive_telephones.common.util.Utils;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.Optional;
import java.util.stream.Stream;

public class SwitchboardTier1TileEntity extends BaseSwitchboardTileEntity {
    public SwitchboardTier1TileEntity() {
        super(TileEntityRegister.SWITCHBOARD_T1.get());
        this.routerCapacity = 4;
    }

    public void testConnection(ServerPlayerEntity player){
        if(world == null) {
            player.sendMessage(new StringTextComponent("world == null"), Util.DUMMY_UUID);
            return;
        }

        Utils.scanConnectedNetworks(world, getPos()).forEach(c -> {
            TileEntity te = world.getTileEntity(c.getPosition());
            if(te instanceof EnergyConnectorTileEntity){
                BlockPos p2 = c.getPosition().offset(((EnergyConnectorTileEntity) te).getFacing());
                TileEntity te2 = world.getTileEntity(p2);
                if(te2 instanceof BaseSwitchboardTileEntity){
                    player.sendMessage(new StringTextComponent(p2 + (p2 == this.getPos() ? " *" : "")), Util.DUMMY_UUID);
                }
            }
        });

        player.sendMessage(new StringTextComponent("======="), Util.DUMMY_UUID);

        Utils.scanConnectedNetworks(world, getPos()).forEach(cnp -> {
            TileEntity te = world.getTileEntity(cnp.getPosition());
            if(te instanceof ICallable) {
                player.sendMessage(new StringTextComponent(((ICallable)te).getID()), Util.DUMMY_UUID);
            }
        });

        player.sendMessage(new StringTextComponent("======="), Util.DUMMY_UUID);

        Stream.of("111", "222", "333", "444").forEach(s -> {
            Optional<ICallable> call = findCallable(s);
            if(call.isPresent()){
                player.sendMessage(new StringTextComponent(s + ": Found " + Stream.of(ICallable.class, IAudioProvider.class, IAudioReceiver.class)
                        .map(type -> msg(call.get(), type))
                        .reduce((a,b) -> a + " " + b).get()), Util.DUMMY_UUID);
            }else{
                player.sendMessage(new StringTextComponent(s + ": Not found"), Util.DUMMY_UUID);
            }
        });

        player.sendMessage(new StringTextComponent("======="), Util.DUMMY_UUID);
        Stream.of(ICallable.class, IAudioProvider.class, IAudioReceiver.class).forEach(type ->
                player.sendMessage(new StringTextComponent(type.getSimpleName() + " " + getCapacityForType(type)), Util.DUMMY_UUID));

    }

    <T> String msg(ICallable call, Class<T> type){
        Optional<T> o = call.getFunctionality(type);
        return o.map(t -> (this.isFunctional(type, t) ? "true" : "false")).orElse("nofunc");
    }

    @Override
    public <T> int getCapacityForType(Class<T> type) {
        int cap = super.getCapacityForType(type);
        if(type == ICallable.class) cap += 2;
        return cap;
    }
}

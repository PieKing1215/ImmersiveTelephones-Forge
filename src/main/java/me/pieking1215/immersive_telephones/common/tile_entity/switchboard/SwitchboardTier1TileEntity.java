package me.pieking1215.immersive_telephones.common.tile_entity.switchboard;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireApi;
import blusunrize.immersiveengineering.api.wires.utils.WireUtils;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorTileEntity;
import me.pieking1215.immersive_telephones.common.tile_entity.ICallable;
import me.pieking1215.immersive_telephones.common.tile_entity.TileEntityRegister;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.stream.Stream;

public class SwitchboardTier1TileEntity extends BaseSwitchboardTileEntity {
    public SwitchboardTier1TileEntity() {
        super(TileEntityRegister.SWITCHBOARD_T1.get());
    }

    public void testConnection(ServerPlayerEntity player){
        if(world == null) {
            player.sendMessage(new StringTextComponent("world == null"), Util.DUMMY_UUID);
            return;
        }

        scanConnectedNetworks().forEach(c -> {
            TileEntity te = world.getTileEntity(c.getPosition());
            if(te instanceof EnergyConnectorTileEntity){
                BlockPos p2 = c.getPosition().offset(((EnergyConnectorTileEntity) te).getFacing());
                TileEntity te2 = world.getTileEntity(p2);
                if(te2 instanceof BaseSwitchboardTileEntity){
                    player.sendMessage(new StringTextComponent(p2.toString() + (p2 == this.getPos() ? " *" : "")), Util.DUMMY_UUID);
                }
            }
        });

        player.sendMessage(new StringTextComponent("======="), Util.DUMMY_UUID);

        scanConnectedNetworks().forEach(cnp -> {
            TileEntity te = world.getTileEntity(cnp.getPosition());
            if(te instanceof ICallable) {
                player.sendMessage(new StringTextComponent(((ICallable)te).getID()), Util.DUMMY_UUID);
            }
        });

        player.sendMessage(new StringTextComponent("======="), Util.DUMMY_UUID);

        Stream.of("111", "222", "333", "444").forEach(s -> {
            if(findTileEntitiesWithType(ICallable.class, s).isPresent()){
                player.sendMessage(new StringTextComponent(s + ": Found"), Util.DUMMY_UUID);
            }else{
                player.sendMessage(new StringTextComponent(s + ": Not found"), Util.DUMMY_UUID);
            }
        });

    }

}

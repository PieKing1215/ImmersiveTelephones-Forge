package me.pieking1215.immersive_telephones.common.command;

import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.pieking1215.immersive_telephones.common.block.IAudioProvider;
import me.pieking1215.immersive_telephones.common.block.IAudioReceiver;
import me.pieking1215.immersive_telephones.common.block.ICallable;
import me.pieking1215.immersive_telephones.common.block.switchboard.BaseSwitchboardTileEntity;
import me.pieking1215.immersive_telephones.common.util.Utils;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

import java.util.Collection;
import java.util.stream.Collectors;

public class ProbeConnectionCommand {
    public static void register(CommandDispatcher<CommandSource> dispatcher){
        LiteralArgumentBuilder<CommandSource> builder = Commands.literal("tel").requires((commandSource) -> commandSource.hasPermissionLevel(2));

        // /tel probe x y z
        builder.then(Commands.literal("probe").then(Commands.argument("position", BlockPosArgument.blockPos()).executes((source) -> {
            BlockPos pos = BlockPosArgument.getBlockPos(source, "position");
            LocalWireNetwork net = GlobalWireNetwork.getNetwork(source.getSource().getWorld()).getNullableLocalNet(pos);
            if(net == null){
                source.getSource().sendFeedback(new StringTextComponent("No LocalWireNetwork at " + pos), true);
                return 1;
            }else{
                source.getSource().sendFeedback(new StringTextComponent("Connections for LocalWireNetwork at " + pos + ":"), true);
                int i = 0;
                for(BlockPos p : net.getConnectors()){
                    String s = i + ") " + p.toString();
                    IImmersiveConnectable connect = net.getConnector(p);
                    s += " : " + connect.getClass().getSimpleName();
                    if(connect instanceof ICallable){
                        ICallable e = (ICallable) connect;
                        s += "(" + e.getID() + ")";
                    }
                    source.getSource().sendFeedback(new StringTextComponent(s), true);
                    i++;
                }
                return 0;
            }
        })));

        builder.then(Commands.literal("switch").then(Commands.argument("position", BlockPosArgument.blockPos()).executes((source) -> {
            BlockPos pos = BlockPosArgument.getBlockPos(source, "position");
            TileEntity te = source.getSource().getWorld().getTileEntity(pos);
            if(te != null){
                Collection<BaseSwitchboardTileEntity> sbs = Utils.findSwitchboards(te).collect(Collectors.toList());
                source.getSource().sendFeedback(new StringTextComponent("There are " + sbs.size() + " switchboards:"), true);
                sbs.forEach(sb -> 
                        source.getSource().sendFeedback(new StringTextComponent(sb.getPos() + " " + sb.getCapacityForType(ICallable.class) + " " + sb.getCapacityForType(IAudioProvider.class) + " " + sb.getCapacityForType(IAudioReceiver.class)), true));
                return 0;
            }else{
                source.getSource().sendFeedback(new StringTextComponent("No TE"), true);
            }

            return 1;
        })));

        dispatcher.register(builder);
    }
}

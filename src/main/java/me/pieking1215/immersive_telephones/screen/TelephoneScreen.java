package me.pieking1215.immersive_telephones.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import me.pieking1215.immersive_telephones.net.ImmersiveTelephonePacketHandler;
import me.pieking1215.immersive_telephones.net.StartCallPacket;
import me.pieking1215.immersive_telephones.tile_entity.TelephoneTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;

public class TelephoneScreen extends Screen {

    private List<TelephoneTileEntity> options;
    private TelephoneTileEntity tileEntity;

    public TelephoneScreen(TelephoneTileEntity tileEntity) {
        super(new StringTextComponent("Telephone Screen"));
        this.tileEntity = tileEntity;
        this.options = tileEntity.findConnectedPhones();
    }

    @Override
    protected void init() {
        super.init();

        for(int i = 0; i < options.size(); i++){
            TelephoneTileEntity opt = options.get(i);
            Button cb = new Button(10, 20 + (i * 22), 100, 20, new StringTextComponent(opt.getName()), (b) -> {
                System.out.println("CALLING " + opt.getName());
                Minecraft.getInstance().displayGuiScreen(null);

                StartCallPacket packet = new StartCallPacket(tileEntity.getPosition(), opt.getName());
                ImmersiveTelephonePacketHandler.INSTANCE.sendToServer(packet);

            });
            addButton(cb);
        }
    }



    @SuppressWarnings("NullableProblems")
    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {

        Minecraft.getInstance().fontRenderer.drawStringWithShadow(matrixStack, "telephone \"" + tileEntity.getName() + "\"" + " " + tileEntity.getUUID().toString(), 10, 10, 0xffffff);

        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

}

package com.elthisboy.calurosanavidad.client.screen;

import com.elthisboy.calurosanavidad.attachment.ModAttachments;
import com.elthisboy.calurosanavidad.attachment.SantaQuestData;
import com.elthisboy.calurosanavidad.menu.SantaMenu;
import com.elthisboy.calurosanavidad.network.SantaActionPayload;
import com.elthisboy.calurosanavidad.quest.SantaQuests;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;


public class SantaScreen extends AbstractContainerScreen<SantaMenu> {

    public SantaScreen(SantaMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 220;
        this.imageHeight = 180;
    }

    @Override
    protected void init() {
        super.init();

        int x = this.leftPos + 10;
        int y = this.topPos + 20;

        // Botones: aceptar cada quest
        int i = 1;
        for (var q : SantaQuests.LIST) {
            int yy = y + i * 22;
            this.addRenderableWidget(Button.builder(
                    Component.translatable("gui.calurosanavidad.santa.accept", q.id()),
                    btn -> PacketDistributor.sendToServer(new SantaActionPayload(menu.getSantaEntityId(), SantaActionPayload.Action.ACCEPT, q.id()))
            ).pos(x, yy).size(200, 20).build());
            i++;
        }

        // Botón reclamar
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.calurosanavidad.santa.claim"),
                btn -> PacketDistributor.sendToServer(new SantaActionPayload(menu.getSantaEntityId(), SantaActionPayload.Action.CLAIM, -1))
        ).pos(this.leftPos + 10, this.topPos + 20 + i * 22 + 6).size(200, 20).build());

        // Botón “regalo”
        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.calurosanavidad.santa.gift"),
                btn -> PacketDistributor.sendToServer(new SantaActionPayload(menu.getSantaEntityId(), SantaActionPayload.Action.GIFT, -1))
        ).pos(this.leftPos + 10, this.topPos + 20 + i * 22 + 30).size(200, 20).build());
    }

    @Override
    protected void renderLabels(net.minecraft.client.gui.GuiGraphics gg, int mouseX, int mouseY) {
        gg.drawString(this.font, this.title, 8, 6, 0xffffff, false);

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        SantaQuestData data = player.getData(ModAttachments.SANTA_DATA.get());
        gg.drawString(this.font, Component.translatable("gui.calurosanavidad.santa.active", data.activeQuestId()), 8, 160, 0xffffff, false);
        gg.drawString(this.font, Component.translatable("gui.calurosanavidad.santa.completed_mask", data.completedMask()), 8, 170, 0xffffff, false);
    }

    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        // MVP: fondo simple (puedes dibujar tu textura aquí)
    }
}
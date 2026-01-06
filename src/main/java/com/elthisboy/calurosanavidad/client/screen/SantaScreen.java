package com.elthisboy.calurosanavidad.client.screen;

import com.elthisboy.calurosanavidad.attachment.ModAttachments;
import com.elthisboy.calurosanavidad.attachment.SantaQuestData;
import com.elthisboy.calurosanavidad.menu.SantaMenu;
import com.elthisboy.calurosanavidad.network.SantaActionPayload;
import com.elthisboy.calurosanavidad.network.SantaClientState;
import com.elthisboy.calurosanavidad.quest.SantaDailyLogic;
import com.elthisboy.calurosanavidad.quest.SantaQuests;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class SantaScreen extends AbstractContainerScreen<SantaMenu> {

    private final List<Button> missionButtons = new ArrayList<>();
    private Button claimBtn;
    private Button giftBtn;
    private Button debugBtn;

    // dónde dibujar textos (coords internas del gui, no pantalla)
    private int statusY = 0;

    // ACK
    private boolean waitingAck = false;
    private int pendingQuestId = -1;
    private int pendingSantaId = -1;

    public SantaScreen(SantaMenu menu, Inventory inv, Component title) {
        super(menu, inv, title);
        this.imageWidth = 220;
        this.imageHeight = 230; // más alto para que no se monten textos
    }

    @Override
    protected void init() {
        super.init();
        this.clearWidgets();
        this.missionButtons.clear();
        this.claimBtn = null;
        this.giftBtn = null;
        this.debugBtn = null;

        int x = this.leftPos + 10;
        int y = this.topPos + 20;
        int btnW = 200;
        int btnH = 20;
        int gap = 4;

        int idx = 0;
        for (var q : SantaQuests.LIST) {
            int yy = y + idx * (btnH + gap);
            Button b = Button.builder(
                    // Mostrar 1..N (questId real sigue 0..N-1)
                    Component.translatable("gui.calurosanavidad.santa.accept", q.id() + 1),
                    btn -> {
                        // esperar ACK: NO cerrar aquí
                        this.waitingAck = true;
                        this.pendingQuestId = q.id();
                        this.pendingSantaId = menu.getSantaEntityId();

                        PacketDistributor.sendToServer(new SantaActionPayload(menu.getSantaEntityId(), SantaActionPayload.Action.ACCEPT, q.id()));
                        updateButtons();
                    }
            ).pos(x, yy).size(btnW, btnH).build();

            this.addRenderableWidget(b);
            this.missionButtons.add(b);
            idx++;
        }

        int claimY = y + idx * (btnH + gap) + 10;
        this.claimBtn = Button.builder(
                Component.translatable("gui.calurosanavidad.santa.claim"),
                btn -> PacketDistributor.sendToServer(new SantaActionPayload(menu.getSantaEntityId(), SantaActionPayload.Action.CLAIM, -1))
        ).pos(x, claimY).size(btnW, btnH).build();
        this.addRenderableWidget(this.claimBtn);

        int giftY = claimY + btnH + 8;
        this.giftBtn = Button.builder(
                Component.translatable("gui.calurosanavidad.santa.gift"),
                btn -> PacketDistributor.sendToServer(new SantaActionPayload(menu.getSantaEntityId(), SantaActionPayload.Action.GIFT, -1))
        ).pos(x, giftY).size(btnW, btnH).build();
        this.addRenderableWidget(this.giftBtn);

        int lastButtonBottom = giftY + btnH;

        // DEBUG button (solo creativo; el server también valida)
        var mc = Minecraft.getInstance();
        var player = mc.player;
        if (player != null && mc.gameMode != null && mc.gameMode.getPlayerMode() == GameType.CREATIVE) {
            int dbgY = lastButtonBottom + 8;
            this.debugBtn = Button.builder(
                    Component.translatable("gui.calurosanavidad.santa.debug_reset"),
                    btn -> PacketDistributor.sendToServer(new SantaActionPayload(menu.getSantaEntityId(), SantaActionPayload.Action.DEBUG_RESET, -1))
            ).pos(x, dbgY).size(btnW, btnH).build();
            this.addRenderableWidget(this.debugBtn);

            lastButtonBottom = dbgY + btnH;
        }

        // Etiquetas siempre debajo del último botón
        this.statusY = (lastButtonBottom - this.topPos) + 10;

        updateButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        // Si estamos esperando ACK, consumir SOLO si calza con lo esperado
        if (waitingAck) {
            var ack = SantaClientState.consumeAckIfMatches(pendingSantaId, pendingQuestId);
            if (ack != null) {
                waitingAck = false;
                if (ack.success()) {
                    this.onClose();
                    return;
                } else {
                    // si falla, re-habilitamos botones
                    updateButtons();
                }
            }
        }

        updateButtons();
    }

    private void updateButtons() {
        var player = Minecraft.getInstance().player;
        if (player == null) return;

        SantaQuestData data = player.getData(ModAttachments.SANTA_DATA.get());
        long nowMs = System.currentTimeMillis();

        // Misiones: deshabilitar si completadas hoy o si estamos esperando ACK
        for (int i = 0; i < missionButtons.size(); i++) {
            Button b = missionButtons.get(i);
            int qid = SantaQuests.LIST.get(i).id();
            b.active = !waitingAck && !data.isCompleted(qid);
        }

        // Claim: solo si hay misión activa y no está completada
        if (claimBtn != null) {
            int active = data.activeQuestId();
            claimBtn.active = !waitingAck && active >= 0 && !data.isCompleted(active);
        }

        // Gift: solo si está listo
        if (giftBtn != null) {
            giftBtn.active = !waitingAck && SantaDailyLogic.isGiftReady(data, nowMs);
        }

        if (debugBtn != null) {
            debugBtn.active = true;
        }
    }

    @Override
    protected void renderLabels(net.minecraft.client.gui.GuiGraphics gg, int mouseX, int mouseY) {
        gg.drawString(this.font, this.title, 8, 6, 0xffffff, false);

        var player = Minecraft.getInstance().player;
        if (player == null) return;

        SantaQuestData data = player.getData(ModAttachments.SANTA_DATA.get());
        long nowMs = System.currentTimeMillis();

        // Regalo diario
        Component giftLine;
        if (SantaDailyLogic.isGiftReady(data, nowMs)) {
            giftLine = Component.translatable("gui.calurosanavidad.santa.daily_gift_ready");
        } else {
            long rem = SantaDailyLogic.giftRemainingMs(data, nowMs);
            giftLine = Component.translatable("gui.calurosanavidad.santa.daily_gift_in", formatHms(rem));
        }
        gg.drawString(this.font, giftLine, 8, this.statusY, 0xffffff, false);

        // Misión activa
        int active = data.activeQuestId();
        Component activeText = (active < 0)
                ? Component.translatable("gui.calurosanavidad.santa.active_none")
                : Component.translatable("gui.calurosanavidad.santa.active", active + 1);
        gg.drawString(this.font, activeText, 8, this.statusY + 10, 0xffffff, false);

        // Completadas X/N
        int completed = 0;
        for (var q : SantaQuests.LIST) {
            if (data.isCompleted(q.id())) completed++;
        }
        gg.drawString(this.font,
                Component.translatable("gui.calurosanavidad.santa.completed", completed, SantaQuests.LIST.size()),
                8, this.statusY + 20, 0xffffff, false);
    }

    private static String formatHms(long ms) {
        long total = Math.max(0L, ms) / 1000L;
        long h = total / 3600L;
        long m = (total % 3600L) / 60L;
        long s = total % 60L;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    @Override
    protected void renderBg(net.minecraft.client.gui.GuiGraphics gg, float partialTick, int mouseX, int mouseY) {
        // MVP: fondo simple (puedes dibujar tu textura aquí)
    }
}

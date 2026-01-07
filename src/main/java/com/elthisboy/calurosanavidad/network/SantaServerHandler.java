package com.elthisboy.calurosanavidad.network;

import com.elthisboy.calurosanavidad.attachment.ModAttachments;
import com.elthisboy.calurosanavidad.attachment.SantaQuestData;
import com.elthisboy.calurosanavidad.menu.SantaMenu;
import com.elthisboy.calurosanavidad.quest.SantaDailyLogic;
import com.elthisboy.calurosanavidad.quest.SantaQuests;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public final class SantaServerHandler {

    public static void handle(SantaActionPayload payload, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (!(ctx.player() instanceof ServerPlayer player)) return;

            // Asegura que el menú correcto esté abierto
            if (!(player.containerMenu instanceof SantaMenu menu)) return;
            if (menu.getSantaEntityId() != payload.santaEntityId()) return;

            Entity e = player.level().getEntity(payload.santaEntityId());
            if (e == null || player.distanceToSqr(e) > 64) return;

            long nowMs = System.currentTimeMillis();

            // Normaliza reset diario (misiones)
            SantaQuestData data0 = player.getData(ModAttachments.SANTA_DATA.get());
            SantaQuestData data = SantaDailyLogic.normalizeQuestReset(data0, nowMs);
            if (data != data0) {
                player.setData(ModAttachments.SANTA_DATA.get(), data);
            }

            switch (payload.action()) {
                case DEBUG_RESET -> {
                    if (player.gameMode.getGameModeForPlayer() != GameType.CREATIVE) return;
                    SantaQuestData reset = new SantaQuestData(-1, 0, nowMs, 0L);
                    player.setData(ModAttachments.SANTA_DATA.get(), reset);
                    player.displayClientMessage(Component.translatable("message.calurosanavidad.santa.debug_reset_done"), false);
                }

                case ACCEPT -> {
                    SantaQuests.Quest q = SantaQuests.byId(payload.questId());
                    if (q == null) {
                        PacketDistributor.sendToPlayer(player, new SantaAckPayload(payload.santaEntityId(), payload.questId(), false));
                        return;
                    }

                    // Si ya está completada hoy, no permitir
                    if (data.isCompleted(q.id())) {
                        player.displayClientMessage(Component.translatable("message.calurosanavidad.santa.already_completed"), false);
                        PacketDistributor.sendToPlayer(player, new SantaAckPayload(payload.santaEntityId(), payload.questId(), false));
                        return;
                    }

                    // Set misión activa
                    SantaQuestData next = data.withActive(q.id());
                    player.setData(ModAttachments.SANTA_DATA.get(), next);

                    // Info de la misión
                    player.displayClientMessage(Component.translatable("message.calurosanavidad.santa.mission_accepted", q.id() + 1), false);
                    player.displayClientMessage(Component.translatable(
                            "message.calurosanavidad.santa.requirements",
                            q.requiredCount(), q.required().getDescription(),
                            q.rewardCount(), q.reward().getDescription()
                    ), false);

                    int coalPct = Math.round(q.coalChance() * 100f);
                    if (coalPct > 0) {
                        player.displayClientMessage(Component.translatable("message.calurosanavidad.santa.coal_chance", coalPct), false);
                    }

                    PacketDistributor.sendToPlayer(player, new SantaAckPayload(payload.santaEntityId(), payload.questId(), true));
                }

                case CLAIM -> {
                    int qid = data.activeQuestId();
                    if (qid < 0) {
                        player.displayClientMessage(Component.translatable("message.calurosanavidad.santa.claim_no_active"), false);
                        return;
                    }

                    SantaQuests.Quest q = SantaQuests.byId(qid);
                    if (q == null) {
                        player.displayClientMessage(Component.translatable("message.calurosanavidad.santa.claim_no_active"), false);
                        return;
                    }

                    if (data.isCompleted(qid)) {
                        player.displayClientMessage(Component.translatable("message.calurosanavidad.santa.already_completed"), false);
                        return;
                    }

                    int have = countItem(player, q.required());
                    if (have < q.requiredCount()) {
                        player.displayClientMessage(Component.translatable(
                                "message.calurosanavidad.santa.missing_items",
                                have, q.requiredCount(), q.required().getDescription()
                        ), false);
                        return;
                    }

                    removeItem(player, q.required(), q.requiredCount());
                    giveReward(player, q.reward(), q.rewardCount(), q.coalChance());

                    // marcar completada y limpiar activa
                    SantaQuestData next = data.markCompleted(qid).clearActive();
                    player.setData(ModAttachments.SANTA_DATA.get(), next);

                    player.displayClientMessage(Component.translatable("message.calurosanavidad.santa.claimed"), false);
                }

                case GIFT -> {
                    // Gift diario con cooldown 24h real
                    if (!SantaDailyLogic.isGiftReady(data, nowMs)) {
                        long rem = SantaDailyLogic.giftRemainingMs(data, nowMs);
                        player.displayClientMessage(
                                Component.translatable("message.calurosanavidad.santa.gift_cooldown", formatHms(rem)),
                                false
                        );

                        // ACK: falló (no cerrar)
                        PacketDistributor.sendToPlayer(player,
                                new SantaAckPayload(payload.santaEntityId(), payload.questId(), false));
                        return;
                    }

                    RandomSource r = player.getRandom();
                    ItemStack gift = r.nextBoolean()
                            ? new ItemStack(Items.COAL, 1)
                            : new ItemStack(Items.COOKIE, 3);
                    player.addItem(gift);

                    SantaQuestData next = SantaDailyLogic.setGiftClaimedNow(data, nowMs);
                    player.setData(ModAttachments.SANTA_DATA.get(), next);

                    player.displayClientMessage(
                            Component.translatable("message.calurosanavidad.santa.gift_received"),
                            false
                    );

                    // ACK: éxito (cerrar)
                    PacketDistributor.sendToPlayer(player,
                            new SantaAckPayload(payload.santaEntityId(), payload.questId(), true));
                }
            }
        });
    }

    private static int countItem(ServerPlayer player, Item item) {
        int total = 0;
        for (var stack : player.getInventory().items) {
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    private static void removeItem(ServerPlayer player, Item item, int amount) {
        int remaining = amount;
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            var stack = player.getInventory().items.get(i);
            if (!stack.is(item)) continue;

            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;

            if (remaining <= 0) break;
        }
    }

    private static void giveReward(ServerPlayer player, Item reward, int rewardCount, float coalChance) {
        RandomSource r = player.getRandom();
        if (r.nextFloat() < coalChance) {
            player.addItem(new ItemStack(Items.COAL, 1));
        } else {
            player.addItem(new ItemStack(reward, rewardCount));
        }
    }

    private static String formatHms(long ms) {
        long total = Math.max(0L, ms) / 1000L;
        long h = total / 3600L;
        long m = (total % 3600L) / 60L;
        long s = total % 60L;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    private SantaServerHandler() {}
}

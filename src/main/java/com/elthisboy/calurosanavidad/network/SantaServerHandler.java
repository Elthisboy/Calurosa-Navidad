package com.elthisboy.calurosanavidad.network;

import com.elthisboy.calurosanavidad.attachment.ModAttachments;
import com.elthisboy.calurosanavidad.attachment.SantaQuestData;
import com.elthisboy.calurosanavidad.menu.SantaMenu;
import com.elthisboy.calurosanavidad.quest.SantaQuests;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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

            SantaQuestData data = player.getData(ModAttachments.SANTA_DATA.get());

            switch (payload.action()) {
                case ACCEPT -> {
                    // Set misión activa
                    player.setData(ModAttachments.SANTA_DATA.get(), data.withActive(payload.questId()));
                }
                case CLAIM -> {
                    int qid = data.activeQuestId();
                    SantaQuests.Quest q = SantaQuests.byId(qid);
                    if (q == null) return;

                    // Ya completada? (si quieres permitir reclamar solo 1 vez)
                    if (data.isCompleted(qid)) return;

                    // Validar items en inventario (server-side)
                    int have = countItem(player, q.required());
                    if (have < q.requiredCount()) return;

                    // Quitar items
                    removeItem(player, q.required(), q.requiredCount());

                    // Recompensa o carbón
                    giveReward(player, q.reward(), q.rewardCount(), q.coalChance());

                    player.setData(ModAttachments.SANTA_DATA.get(), data.markCompleted(qid));
                }
                case GIFT -> {
                    long day = player.level().getDayTime() / 24000L;
                    if (data.lastGiftDay() == day) return;

                    // 50/50: carbón o galletas (cámbialo)
                    RandomSource r = player.getRandom();
                    ItemStack gift = r.nextBoolean() ? new ItemStack(Items.COAL, 1) : new ItemStack(Items.COOKIE, 3);
                    player.addItem(gift);

                    player.setData(ModAttachments.SANTA_DATA.get(), data.withGiftDay(day));
                }
            }
        });
    }

    private static int countItem(ServerPlayer player, net.minecraft.world.item.Item item) {
        int total = 0;
        for (var stack : player.getInventory().items) {
            if (stack.is(item)) total += stack.getCount();
        }
        return total;
    }

    private static void removeItem(ServerPlayer player, net.minecraft.world.item.Item item, int amount) {
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

    private static void giveReward(ServerPlayer player, net.minecraft.world.item.Item reward, int rewardCount, float coalChance) {
        RandomSource r = player.getRandom();
        if (r.nextFloat() < coalChance) {
            player.addItem(new ItemStack(Items.COAL, 1));
        } else {
            player.addItem(new ItemStack(reward, rewardCount));
        }
    }

    private SantaServerHandler() {}
}
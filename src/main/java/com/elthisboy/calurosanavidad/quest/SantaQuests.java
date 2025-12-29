package com.elthisboy.calurosanavidad.quest;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.List;

public final class SantaQuests {

    public record Quest(int id, Item required, int requiredCount, Item reward, int rewardCount, float coalChance) {}

    // IDs 0..30 para calzar con bitmask
    public static final List<Quest> LIST = List.of(
            new Quest(0, Items.SUGAR, 16, Items.COOKIE, 6, 0.25f),
            new Quest(1, Items.COCOA_BEANS, 12, Items.EMERALD, 2, 0.20f),
            new Quest(2, Items.MILK_BUCKET, 1, Items.CAKE, 1, 0.30f)
    );

    public static Quest byId(int id) {
        return LIST.stream().filter(q -> q.id == id).findFirst().orElse(null);
    }

    private SantaQuests() {}
}
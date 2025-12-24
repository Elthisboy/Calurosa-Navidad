package com.elthisboy.calurosanavidad.item.custom;

import net.minecraft.world.item.Item;

public class WaterGunItem extends WaterPistolItem {

    public WaterGunItem(Item.Properties props) {
        super(props);
    }

    // “Water Gun” más potente (ajusta a gusto)
    @Override protected int maxWater() { return 32; }
    @Override protected int maxPressure() { return 6; }
    @Override protected double range() { return 12.0; }
    @Override protected int streamIntervalTicks() { return 1; }
    @Override protected int burstIntervalTicks() { return 4; }
    @Override protected double pushStrength() { return 0.55; }
}

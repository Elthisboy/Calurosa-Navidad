package com.elthisboy.calurosanavidad.block.custom;

import net.minecraft.util.StringRepresentable;

public enum PoolVariant implements StringRepresentable {
    SINGLE("single"),

    END_N("end_n"),
    END_E("end_e"),
    END_S("end_s"),
    END_W("end_w"),

    CORNER_NE("corner_ne"),
    CORNER_NW("corner_nw"),
    CORNER_SE("corner_se"),
    CORNER_SW("corner_sw"),

    STRAIGHT_NS("straight_ns"),
    STRAIGHT_EW("straight_ew");

    private final String id;

    PoolVariant(String id) {
        this.id = id;
    }

    @Override
    public String getSerializedName() {
        return id;
    }
}

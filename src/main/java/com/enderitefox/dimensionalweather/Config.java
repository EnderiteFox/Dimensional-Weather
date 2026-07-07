package com.enderitefox.dimensionalweather;

import net.neoforged.neoforge.common.ModConfigSpec;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Neo's config APIs
public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue OBLIVION_FILL_RATE = BUILDER
        .comment("How much the Oblivion bar fills each second")
        .defineInRange("oblivionFillRate", 0.4, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue OBLIVION_EMPTY_RATE = BUILDER
        .comment("How much the Oblvion bar empties each second")
        .defineInRange("oblivionEmptyRate", 0.2, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue OBLIVION_DAMAGE = BUILDER
        .comment("Damage per second once the Oblivion bar is full")
        .defineInRange("oblivionDamage", 2.0, 0.0, Double.MAX_VALUE);

    static final ModConfigSpec SPEC = BUILDER.build();
}

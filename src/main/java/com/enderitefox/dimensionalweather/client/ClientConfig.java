package com.enderitefox.dimensionalweather.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class ClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue OBLIVION_BAR_X_POSITION = BUILDER
        .comment("The X position of the Oblivion bar")
        .defineInRange("oblivionBarXPos", 0.1, 0.0, 1.0);

    public static final ModConfigSpec.DoubleValue OBLIVION_BAR_Y_POSITION = BUILDER
        .comment("The Y position of the Oblivion bar")
        .defineInRange("oblivionBarYPos", 0.5, 0.0, 1.0);

    public static final ModConfigSpec SPEC = BUILDER.build();
}

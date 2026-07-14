package com.enderitefox.dimensionalweather.client.oblivion;

import com.enderitefox.dimensionalweather.Config;
import com.enderitefox.dimensionalweather.DimensionalWeather;
import com.enderitefox.dimensionalweather.client.ClientConfig;
import com.enderitefox.dimensionalweather.weather.OblivionWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = DimensionalWeather.MODID, value = Dist.CLIENT)
public class OblivionChargeBar {
    private static final Identifier BAR_TEXTURE = Identifier.fromNamespaceAndPath(
        DimensionalWeather.MODID,
        "textures/gui/oblivion_bar.png"
    );
    private static final float BAR_TEXTURE_SCALE = 0.65f;
    private static final int BAR_TEXTURE_WIDTH = (int) (48 * BAR_TEXTURE_SCALE);
    private static final int BAR_TEXTURE_HEIGHT = (int) (256 * BAR_TEXTURE_SCALE);

    private static final int BAR_INSIDE_RECT_X = (int) (13 * BAR_TEXTURE_SCALE);
    private static final int BAR_INSIDE_RECT_WIDTH = (int) (24 * BAR_TEXTURE_SCALE);
    private static final int BAR_INSIDE_RECT_HEIGHT = (int) (235 * BAR_TEXTURE_SCALE);

    private static final int BAR_FILL_COLOR = 0x00aa00b6;

    private static final double TRANSPARENCY_TWEEN_RATE = 2.5;

    private static double transparency = 0.0;

    private static int getTransparency() {
        return ((int) (0xb0 * transparency)) << 24;
    }

    private static int getFillColor() {
        return BAR_FILL_COLOR | getTransparency();
    }

    private static int getModulateColor() {
        return 0x00ffffff | getTransparency();
    }


    public static double currentVal = 0.0;

    @SubscribeEvent
    public static void onGuiRender(RenderGuiEvent.Post event) {
        if (Minecraft.getInstance().gui.hud.isHidden()) {
            return;
        }

        ClientLevel level = Minecraft.getInstance().level;
        if (level == null) {
            return;
        }

        final Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        if (!Minecraft.getInstance().isPaused()) {
            final double tickRateRatio = level.tickRateManager().tickrate() / 20.0;
            final double delta = (event.getPartialTick().getRealtimeDeltaTicks() / 20.0f) * tickRateRatio;

            final double charge = player.getData(DimensionalWeather.OBLIVION_CHARGE);
            final double chargeFillRate = OblivionWeather.aboveVoid ? Config.OBLIVION_FILL_RATE.get() : -Config.OBLIVION_FILL_RATE.get();
            final double chargeFillDelta = chargeFillRate * delta;
            currentVal = Math.clamp(currentVal + chargeFillDelta, 0.0, 1.0);

            final double transparencyFillRate = TRANSPARENCY_TWEEN_RATE * delta * (charge != 0 ? 1.0 : -1.0);
            transparency = Math.clamp(transparency + transparencyFillRate, 0.0, 1.0);
        }

        if (transparency == 0.0) {
            return;
        }

        final GuiGraphicsExtractor graphics = event.getGuiGraphics();
        final int guiWidth = graphics.guiWidth();
        final int guiHeight = graphics.guiHeight();

        final int barX = (int) (guiWidth * ClientConfig.OBLIVION_BAR_X_POSITION.get());
        final int barY = (int) (guiHeight * ClientConfig.OBLIVION_BAR_Y_POSITION.get());
        final int filledHeight = (int) (BAR_INSIDE_RECT_HEIGHT * currentVal);

        graphics.blit(
            RenderPipelines.GUI_TEXTURED,
            BAR_TEXTURE,
            barX - BAR_TEXTURE_WIDTH / 2,
            barY - BAR_TEXTURE_HEIGHT / 2,
            0,
            0,
            BAR_TEXTURE_WIDTH,
            BAR_TEXTURE_HEIGHT,
            BAR_TEXTURE_WIDTH,
            BAR_TEXTURE_HEIGHT,
            getModulateColor()
        );

        final int insideX = barX - BAR_TEXTURE_WIDTH / 2 + BAR_INSIDE_RECT_X;
        graphics.fill(
            insideX,
            barY - filledHeight / 2,
            insideX + BAR_INSIDE_RECT_WIDTH,
            barY + filledHeight / 2,
            getFillColor()
        );
    }
}

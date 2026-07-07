package com.enderitefox.dimensionalweather.client.oblivion;

import com.enderitefox.dimensionalweather.DimensionalWeather;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = DimensionalWeather.MODID, value = Dist.CLIENT)
public class OblivionChargeBar {
    private static final int BAR_WIDTH = 6;
    private static final double BAR_HEIGHT = 0.4;
    private static final int OUTLINE_WIDTH = 2;
    private static final int OUTLINE_MARGIN = 2;

    private static final double X_POSITION = 0.2;
    private static final double Y_POSITION = 0.5;

    @SubscribeEvent
    public static void onGuiRender(RenderGuiEvent.Post event) {
        if (Minecraft.getInstance().gui.hud.isHidden()) {
            return;
        }

        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        final double charge = player.getData(DimensionalWeather.OBLIVION_CHARGE);

        if (charge == 0.0) {
            return;
        }

        final GuiGraphicsExtractor graphics = event.getGuiGraphics();
        final int guiWidth = graphics.guiWidth();
        final int guiHeight = graphics.guiHeight();


        final int barX = (int) (guiWidth * X_POSITION);
        final int barY = (int) (guiHeight * Y_POSITION);
        final int barHeight = (int) (guiHeight * BAR_HEIGHT);
        final int filledHeight = (int) (barHeight * charge);
        final int barWidth = BAR_WIDTH;

        graphics.fill(
            barX - barWidth / 2,
            barY - filledHeight  / 2,
            barX + barWidth / 2,
            barY + filledHeight / 2,
            0xFFFFFFFF
        );

        graphics.outline(
            barX - barWidth / 2 - OUTLINE_MARGIN - OUTLINE_WIDTH,
            barY - barHeight / 2 - OUTLINE_MARGIN - OUTLINE_WIDTH,
            barWidth + 2 * OUTLINE_MARGIN + 2 * OUTLINE_WIDTH,
            barHeight + 2 * OUTLINE_MARGIN + 2 * OUTLINE_WIDTH,
            0xFFFFFFFF
        );
    }
}

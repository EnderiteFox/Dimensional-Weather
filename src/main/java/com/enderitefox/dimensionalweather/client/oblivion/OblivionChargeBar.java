package com.enderitefox.dimensionalweather.client.oblivion;

import com.enderitefox.dimensionalweather.DimensionalWeather;
import com.enderitefox.dimensionalweather.client.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

    private static final int BAR_TRANSPARENCY = 0xb0 << 24;
    private static final int BAR_FILL_COLOR = 0x00aa00b6 | BAR_TRANSPARENCY;

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


        final int barX = (int) (guiWidth * ClientConfig.OBLIVION_BAR_X_POSITION.get());
        final int barY = (int) (guiHeight * ClientConfig.OBLIVION_BAR_Y_POSITION.get());
        final int filledHeight = (int) (BAR_INSIDE_RECT_HEIGHT * charge);

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
            0x00ffffff | BAR_TRANSPARENCY
        );

        final int insideX = barX - BAR_TEXTURE_WIDTH / 2 + BAR_INSIDE_RECT_X;
        graphics.fill(
            insideX,
            barY - filledHeight / 2,
            insideX + BAR_INSIDE_RECT_WIDTH,
            barY + filledHeight / 2,
            BAR_FILL_COLOR
        );
    }
}

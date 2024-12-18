package archives.tater.omnicrossbow.client.render.gui.hud;

import archives.tater.omnicrossbow.MultichamberedEnchantment;
import archives.tater.omnicrossbow.OmniCrossbow;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

public class MultichamberedIndicator {
    public static final Identifier FILLED_ARROW = new Identifier(OmniCrossbow.MOD_ID, "textures/gui/arrow_loaded.png");
    public static final Identifier EMPTY_ARROW = new Identifier(OmniCrossbow.MOD_ID, "textures/gui/arrow_unloaded.png");

    private static @Nullable ItemStack lastCrossbow = null;
    private static int lastCount = -1;
    private static float displayTicks = 0;

    public static final float INDICATOR_DISPLAY_TICKS = 200;

    public static void drawIndicator(DrawContext drawContext, float tickDelta) {
        if (MinecraftClient.getInstance().interactionManager == null || MinecraftClient.getInstance().interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) return;
        if (!(MinecraftClient.getInstance().getCameraEntity() instanceof LivingEntity livingEntity)) return;
        var crossbow = MultichamberedEnchantment.getPrimaryCrossbow(livingEntity);
        if (crossbow.isEmpty()) {
            lastCrossbow = null;
            return;
        }

        if (lastCrossbow != crossbow) {
            lastCrossbow = crossbow;
            displayTicks = INDICATOR_DISPLAY_TICKS;
        }

        var loadedShots = MultichamberedEnchantment.getLoadedShots(crossbow);

        if (lastCount != loadedShots) {
            lastCount = loadedShots;
            displayTicks = INDICATOR_DISPLAY_TICKS;
        }

        if (displayTicks <= 0) return;
        displayTicks -= tickDelta;

        var maxShots = MultichamberedEnchantment.getMaxShots(crossbow);

        RenderSystem.enableBlend();

        for (int i = 0; i < maxShots; i++)
            drawContext.drawTexture(i < loadedShots ? FILLED_ARROW : EMPTY_ARROW,
                    drawContext.getScaledWindowWidth() / 2 + 9 * i - maxShots * 9 / 2,
                    drawContext.getScaledWindowHeight() / 2 + 16,
                    0,
                    0,
                    8,
                    8,
                    8,
                    8);

        RenderSystem.disableBlend();
    }
}

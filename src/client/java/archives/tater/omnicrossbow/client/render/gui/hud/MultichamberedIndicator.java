package archives.tater.omnicrossbow.client.render.gui.hud;

import archives.tater.omnicrossbow.MultichamberedEnchantment;
import archives.tater.omnicrossbow.MultichamberedIndicatorTracker.MaxShotsChangedPayload;
import archives.tater.omnicrossbow.OmniCrossbow;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.WeakHashMap;

import static java.lang.Math.max;

public class MultichamberedIndicator {
    public static final Identifier FILLED_ARROW = OmniCrossbow.id("textures/gui/arrow_loaded.png");
    public static final Identifier EMPTY_ARROW = OmniCrossbow.id("textures/gui/arrow_unloaded.png");

    private static @Nullable ItemStack lastCrossbow = null;
    private static int lastCount = -1;
    private static float displayTicks = 0;

    public static final float INDICATOR_DISPLAY_TICKS = 200;

    private static final Map<ItemStack, Integer> stackMaxShots = new WeakHashMap<>();

    public static void drawIndicator(DrawContext drawContext, RenderTickCounter tickCounter) {
        if (MinecraftClient.getInstance().interactionManager == null || MinecraftClient.getInstance().interactionManager.getCurrentGameMode() == GameMode.SPECTATOR) return;

        var crossbow = MultichamberedEnchantment.getPrimaryCrossbow(MinecraftClient.getInstance().player);
        if (crossbow.isEmpty() || !stackMaxShots.containsKey(crossbow)) {
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
        displayTicks -= tickCounter.getTickDelta(false);

        var maxShots = stackMaxShots.get(crossbow);

        RenderSystem.enableBlend();

        for (int i = 0; i < max(maxShots, loadedShots); i++)
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

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(MaxShotsChangedPayload.ID, (payload, context) ->
                stackMaxShots.put(context.player().getStackInHand(payload.hand()), payload.shots()));

        HudRenderCallback.EVENT.register(MultichamberedIndicator::drawIndicator);
    }
}

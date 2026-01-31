package archives.tater.omnicrossbow.client.render

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.enchantment.ChargedProjectileIndicator
import archives.tater.omnicrossbow.registry.OmniCrossbowComponents
import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantmentEffects
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.core.component.DataComponents
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.enchantment.EnchantmentHelper
import kotlin.math.max

object ChargedProjectileIndicatorRenderer : HudElement {
    val ARROW_EMPTY = OmniCrossbow.id("hud/arrow_empty")
    val ARROW_FULL = OmniCrossbow.id("hud/arrow_full")

    const val ICON_WIDTH = 8
    const val ICON_HEIGHT = 8
    const val GAP = 2
    const val Y_MARGIN = 12

    val ID = OmniCrossbow.id("charged_projectile_indicator")

    override fun render(
        graphics: GuiGraphics,
        deltaTracker: DeltaTracker
    ) {
        val player = Minecraft.getInstance().player ?: return
        val crossbow = InteractionHand.entries
            .map { player.getItemInHand(it) }
            .firstOrNull { EnchantmentHelper.has(it, OmniCrossbowEnchantmentEffects.CHARGED_PROJECTILE_INDICATOR) }
            ?: return
        val maxDisplay = ChargedProjectileIndicator.maxProjectilesOrDefault(crossbow)
        val charged = (crossbow[DataComponents.CHARGED_PROJECTILES]?.takeUnless { it.isEmpty } ?: crossbow[OmniCrossbowComponents.ADDITIONAL_CHARGED_PROJECTILES])?.items?.size ?: 0
        val width = max(maxDisplay, charged)
        val x = graphics.guiWidth() / 2 - (width * (ICON_WIDTH + GAP) - GAP) / 2
        val y = graphics.guiHeight() / 2 + Y_MARGIN
        repeat(width) {
            graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                if (it < charged) ARROW_FULL else ARROW_EMPTY,
                x + (ICON_WIDTH + GAP) * it,
                y,
                ICON_WIDTH,
                ICON_WIDTH
            )
        }
    }
}
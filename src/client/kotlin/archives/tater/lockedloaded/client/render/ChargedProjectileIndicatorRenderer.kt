package archives.tater.lockedloaded.client.render

import archives.tater.lockedloaded.LockedLoaded
import archives.tater.lockedloaded.enchantment.ChargedProjectileIndicator
import archives.tater.lockedloaded.registry.LockedLoadedComponents
import archives.tater.lockedloaded.registry.LockedLoadedEnchantmentEffects
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.minecraft.client.DeltaTracker
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.core.component.DataComponents
import net.minecraft.world.InteractionHand
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import kotlin.math.max

object ChargedProjectileIndicatorRenderer : HudElement, ClientTickEvents.EndLevelTick, ClientPlayConnectionEvents.Disconnect {
    val ARROW_EMPTY = LockedLoaded.id("hud/arrow_empty")
    val ARROW_FULL = LockedLoaded.id("hud/arrow_full")

    const val ICON_WIDTH = 8
    const val ICON_HEIGHT = 8
    const val GAP = 2
    const val Y_MARGIN = 12

    val ID = LockedLoaded.id("charged_projectile_indicator")

    const val DISPLAY_TICKS = 15 * 20

    private var crossbow: ItemStack? = null
    private var displayTicks: Int = 0

    fun register() {
        HudElementRegistry.addLast(ID, this)
        ClientTickEvents.END_LEVEL_TICK.register(this)
        ClientPlayConnectionEvents.DISCONNECT.register(this)
    }

    override fun extractRenderState(
        graphics: GuiGraphicsExtractor,
        deltaTracker: DeltaTracker
    ) {
        if (displayTicks <= 0) return
        val crossbow = crossbow ?: return
        val maxDisplay = ChargedProjectileIndicator.maxProjectilesOrDefault(crossbow)
        val charged = (crossbow[DataComponents.CHARGED_PROJECTILES]?.takeUnless { it.isEmpty } ?: crossbow[LockedLoadedComponents.ADDITIONAL_CHARGED_PROJECTILES])?.items?.size ?: 0
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

    override fun onEndTick(level: ClientLevel) {
        if (displayTicks > 0) displayTicks--

        val player = Minecraft.getInstance().player ?: run {
            crossbow = null
            displayTicks = 0
            return
        }

        val crossbow = InteractionHand.entries
            .map { player.getItemInHand(it) }
            .firstOrNull { EnchantmentHelper.has(it, LockedLoadedEnchantmentEffects.CHARGED_PROJECTILE_INDICATOR) }

        if (crossbow != this.crossbow) {
            displayTicks = DISPLAY_TICKS
            this.crossbow = crossbow
        }
    }

    override fun onPlayDisconnect(listener: ClientPacketListener, client: Minecraft) {
        displayTicks = 0
        crossbow = null
    }
}
package archives.tater.lockedloaded.client.render

import archives.tater.lockedloaded.registry.LockedLoadedEnchantmentEffects
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.core.component.DataComponents
import net.minecraft.util.Mth.TWO_PI
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ChargedProjectiles
import net.minecraft.world.item.enchantment.EnchantmentHelper
import org.joml.Quaternionf

object CrossbowSpinRendering {

    @JvmField val SPINNING_ITEM: RenderStateDataKey<Boolean> = RenderStateDataKey.create()

    private val ARM_TILT_RIGHT = Quaternionf().apply {
        rotationY(getCrossbowSpinTilt(false))
    }

    private val ARM_TILT_LEFT = Quaternionf().apply {
        rotationY(getCrossbowSpinTilt(true))
    }

    private fun getCrossbowSpinTilt(leftHand: Boolean): Float = -TWO_PI * 2 / 12 * (if (leftHand) -1 else 1)

    @JvmStatic
    fun transformCrossbowSpinTilt(part: ModelPart, leftHand: Boolean) {
        part.rotateBy(if (leftHand) ARM_TILT_LEFT else ARM_TILT_RIGHT)
    }

    @JvmStatic
    fun transformCrossbowSpinInHand(
        poseStack: PoseStack,
        ticksUsingItem: Float,
        leftHand: Boolean,
    ) {
        poseStack.translate(0f, 0f, -5f / 16)
        transformCrossbowSpin(poseStack, ticksUsingItem, leftHand, true)
    }

    @JvmStatic
    fun transformCrossbowSpinModel(
        poseStack: PoseStack,
        ticksUsingItem: Float,
        leftHand: Boolean,
    ) {
        poseStack.translate(0f, 0f, -3f / 16)
        transformCrossbowSpin(poseStack, ticksUsingItem, leftHand, false)
        poseStack.translate(0f, 0f, 3f / 16)
    }

    private fun transformCrossbowSpin(
        poseStack: PoseStack,
        ticksUsingItem: Float,
        leftHand: Boolean,
        tilt: Boolean,
    ) {
        val f = if (leftHand) -1 else 1

        poseStack.translate(0.5f / 16 * f, 0f, 5f / 16)
        if (tilt) poseStack.mulPose(Axis.ZN.rotation(getCrossbowSpinTilt(leftHand)))
        poseStack.mulPose(Axis.YP.rotation(-3f * TWO_PI / 20 * ticksUsingItem * f))
        poseStack.translate(-0.5f / 16 * f, 0f, -5f / 16)
    }

    @JvmStatic
    fun shouldSpin(crossbow: ItemStack) = EnchantmentHelper.has(crossbow, LockedLoadedEnchantmentEffects.CROSSBOW_SPIN)
            && !crossbow.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).isEmpty

}
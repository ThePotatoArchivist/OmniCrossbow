package archives.tater.omnicrossbow.client.render.item

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import com.mojang.serialization.MapCodec
import net.minecraft.client.Minecraft
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.item.*
import net.minecraft.client.renderer.item.properties.conditional.IsUsingItem
import net.minecraft.client.renderer.item.properties.numeric.CrossbowPull
import net.minecraft.client.renderer.item.properties.select.Charge
import net.minecraft.client.renderer.special.SpecialModelRenderer
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.function.Consumer

class OmniAmmoRenderer(val itemModelResolver: ItemModelResolver) : SpecialModelRenderer<ItemStackRenderState> {
    override fun submit(
        argument: ItemStackRenderState?,
        type: ItemDisplayContext,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        lightCoords: Int,
        overlayCoords: Int,
        hasFoil: Boolean,
        outlineColor: Int
    ) {
        if (argument == null) return
        poseStack.pushPose()
        poseStack.translate(8 / 16f, 8 / 16f, 9 / 16f)
        poseStack.mulPose(Axis.YP.rotationDegrees(180f))
        poseStack.mulPose(Axis.ZP.rotationDegrees(-90f))
        argument.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor)
        poseStack.popPose()
    }

    override fun getExtents(output: Consumer<Vector3fc>) {
        repeat(2) { x ->
            repeat(2) { y ->
                repeat(2) { z ->
                    output.accept(Vector3f(
                        if (x == 0) -3 / 16f else 13 / 16f,
                        if (y == 0) 3 / 16f else 19 / 16f,
                        if (z == 0) 1 / 16f else 17 / 16f
                    ))
                }
            }
        }
    }

    override fun extractArgument(stack: ItemStack): ItemStackRenderState? =
        stack.get(DataComponents.CHARGED_PROJECTILES)?.items?.firstOrNull()?.create()?.let { projectile ->
            ItemStackRenderState().also {
                itemModelResolver.updateForTopItem(
                    it,
                    projectile,
                    ItemDisplayContext.FIXED,
                    null,
                    null,
                    0
                )
            }
        }

    object Unbaked : SpecialModelRenderer.Unbaked {
        override fun bake(context: SpecialModelRenderer.BakingContext): SpecialModelRenderer<*> =
            OmniAmmoRenderer(Minecraft.getInstance().itemModelResolver)

        override fun type(): MapCodec<out SpecialModelRenderer.Unbaked> = CODEC

        val CODEC: MapCodec<Unbaked> = MapCodec.unit(this)
    }

    companion object {
        private fun findPulling(model: ItemModel.Unbaked): BlockModelWrapper.Unbaked? = when (model) {
            is RangeSelectItemModel.Unbaked if model.property is CrossbowPull -> findPulling(model.entries.last().model)
            is RangeSelectItemModel.Unbaked -> findPulling(model.fallback.orElseGet { model.entries.first().model })
            is SelectItemModel.Unbaked -> findPulling(model.fallback.orElseGet { model.unbakedSwitch.cases.first().model })
            is ConditionalItemModel.Unbaked if model.property is IsUsingItem -> findPulling(model.onTrue)
            is ConditionalItemModel.Unbaked -> findPulling(model.onFalse)
            is CompositeModel.Unbaked -> findPulling(model.models.first())
            is BlockModelWrapper.Unbaked -> model
            else -> null
        }

        fun wrapModel(model: ItemModel.Unbaked): ItemModel.Unbaked {
            val reference = findPulling(model) ?: return model
            return ItemModelUtils.select(Charge(), model,
                ItemModelUtils.`when`(CrossbowItem.ChargeType.ARROW, ItemModelUtils.composite(
                    reference,
                    SpecialModelWrapper.Unbaked(reference.model, Unbaked)
                ))
            )
        }
    }
}

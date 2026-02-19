package archives.tater.omnicrossbow.client.render.item

import archives.tater.omnicrossbow.client.render.AmmoPosition
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.serialization.MapCodec
import net.minecraft.client.Minecraft
import net.minecraft.client.data.models.model.ItemModelUtils
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.block.model.ItemTransform
import net.minecraft.client.renderer.item.*
import net.minecraft.client.renderer.item.properties.conditional.IsUsingItem
import net.minecraft.client.renderer.item.properties.numeric.CrossbowPull
import net.minecraft.client.renderer.special.SpecialModelRenderer
import net.minecraft.core.component.DataComponents
import net.minecraft.util.Mth.PI
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import org.joml.Quaternionf
import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.function.Consumer

class OmniAmmoRenderer(val itemModelResolver: ItemModelResolver) : SpecialModelRenderer<OmniAmmoRenderer.State> {
    override fun submit(
        argument: State?,
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
        poseStack.translate(0.5f, 0.5f, 0.5f)
        argument.transform.apply(false, poseStack.last())
        poseStack.translate(0.5f, 0.5f, 0.5f)
        poseStack.mulPose(Quaternionf().rotationXYZ(0f, PI, 0f))
        argument.stack.submit(poseStack, submitNodeCollector, lightCoords, overlayCoords, outlineColor)
        poseStack.popPose()
    }

    override fun getExtents(output: Consumer<Vector3fc>) {
        repeat(2) { x ->
            repeat(2) { y ->
                repeat(2) { z ->
                    output.accept(Vector3f(
                        if (x == 0) -1f else 1f,
                        if (y == 0) 0f else 2f,
                        if (z == 0) 0f else 1f
                    ))
                }
            }
        }
    }

    override fun extractArgument(stack: ItemStack): State? =
        stack.get(DataComponents.CHARGED_PROJECTILES)?.items?.firstOrNull()?.create()?.let { projectile ->
            val positionEntry = AmmoPosition[Minecraft.getInstance().connection!!.registryAccess(), projectile.item]
            State(
                ItemStackRenderState().also {
                    itemModelResolver.updateForTopItem(
                        it,
                        projectile,
                        positionEntry.displayContext,
                        null,
                        null,
                        0
                    )
                },
                positionEntry.transform
            )
        }

    @JvmRecord
    data class State(
        val stack: ItemStackRenderState,
        val transform: ItemTransform,
    )

    object Unbaked : SpecialModelRenderer.Unbaked {
        override fun bake(context: SpecialModelRenderer.BakingContext): SpecialModelRenderer<*> =
            OmniAmmoRenderer(Minecraft.getInstance().itemModelResolver)

        override fun type(): MapCodec<out SpecialModelRenderer.Unbaked> = CODEC

        val CODEC: MapCodec<Unbaked> = MapCodec.unit(this)
    }

    companion object {
        private fun findPulling(model: ItemModel.Unbaked): ItemModel.Unbaked? = when (model) {
            is RangeSelectItemModel.Unbaked if model.property is CrossbowPull -> model.entries.last().model
            is RangeSelectItemModel.Unbaked -> findPulling(model.fallback.orElseGet { model.entries.first().model })
            is SelectItemModel.Unbaked -> findPulling(model.fallback.orElseGet { model.unbakedSwitch.cases.first().model })
            is ConditionalItemModel.Unbaked if model.property is IsUsingItem -> findPulling(model.onTrue)
            is ConditionalItemModel.Unbaked -> findPulling(model.onFalse)
            is CompositeModel.Unbaked -> findPulling(model.models.first())
            else -> null
        }

        private fun findDefault(model: ItemModel.Unbaked): BlockModelWrapper.Unbaked? = when (model) {
            is RangeSelectItemModel.Unbaked -> findDefault(model.fallback.orElseGet { model.entries.first().model })
            is SelectItemModel.Unbaked -> findDefault(model.fallback.orElseGet { model.unbakedSwitch.cases.first().model })
            is ConditionalItemModel.Unbaked -> findDefault(model.onFalse)
            is CompositeModel.Unbaked -> findDefault(model.models.first())
            is BlockModelWrapper.Unbaked -> model
            else -> null
        }

        fun wrapModel(model: ItemModel.Unbaked): ItemModel.Unbaked {
            val pulling = findPulling(model) ?: return model
            val reference = findDefault(pulling) ?: return model
            return ItemModelUtils.conditional(IsOmniAmmo,
                ItemModelUtils.composite(
                    pulling,
                    SpecialModelWrapper.Unbaked(reference.model, Unbaked)
                ),
                model,
            )
        }
    }
}

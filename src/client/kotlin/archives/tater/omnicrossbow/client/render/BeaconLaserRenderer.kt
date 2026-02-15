package archives.tater.omnicrossbow.client.render

import archives.tater.omnicrossbow.client.util.get
import archives.tater.omnicrossbow.client.util.getOrCreate
import archives.tater.omnicrossbow.projectilebehavior.BeaconLaser
import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments
import archives.tater.omnicrossbow.util.contains
import archives.tater.omnicrossbow.util.get
import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState
import net.fabricmc.fabric.api.client.rendering.v1.LivingEntityRenderLayerRegistrationCallback
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelExtractionContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.model.EntityModel
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BeaconRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.util.Mth
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity

object BeaconLaserRenderer : LivingEntityRenderLayerRegistrationCallback, LevelRenderEvents.EndExtraction, LevelRenderEvents.AfterEntities {

    @JvmField val BEACON_LASER: RenderStateDataKey<State> = RenderStateDataKey.create()
    @JvmField val CAMERA_BEACON_LASER: RenderStateDataKey<State> = RenderStateDataKey.create()

    fun render(state: State, poseStack: PoseStack, submitNodeCollector: SubmitNodeCollector) {
        val thickness = state.thickness
        if (thickness <= 0) return

        poseStack.pushPose()

        poseStack.translate(0f, 1.5f, 0f)

        poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot))
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot - 90))

        poseStack.translate(-0.5f, 0f, -0.5f)

        BeaconRenderer.submitBeaconBeam(
            poseStack,
            submitNodeCollector,
            BeaconRenderer.BEAM_LOCATION,
            1f,
            state.animationTime,
            0,
            state.distance,
            0xFFFFFF,
            thickness,
            thickness
        )

        poseStack.popPose()
    }

    override fun endExtraction(context: LevelExtractionContext) {
        val cameraEntity = context.camera().entity() as? LivingEntity ?: return
        State.extractFrom(cameraEntity, context.levelState(), CAMERA_BEACON_LASER, context.deltaTracker().getGameTimeDeltaPartialTick(false))
    }

    override fun afterEntities(context: LevelRenderContext) {
        val beaconState = context.levelState()[CAMERA_BEACON_LASER] ?: return
        render(beaconState, context.poseStack(), context.submitNodeCollector())
    }

    @Suppress("UNCHECKED_CAST")
    override fun registerLayers(
        entityType: EntityType<out LivingEntity>,
        entityRenderer: LivingEntityRenderer<*, *, *>,
        registrationHelper: LivingEntityRenderLayerRegistrationCallback.RegistrationHelper,
        context: EntityRendererProvider.Context
    ) {
        registrationHelper.register(Layer(entityRenderer as LivingEntityRenderer<*, LivingEntityRenderState, EntityModel<LivingEntityRenderState>>))
    }

    class State {
        var animationTime: Float = 0f
        var distance: Int = 0
        var thickness: Float = 0f
        var xRot: Float = 0f
        var yRot: Float = 0f

        fun extractFrom(display: BeaconLaser.Display, entity: LivingEntity, partialTickTime: Float) {
            animationTime = display.getAnimationTime(partialTickTime)
            distance = display.distance
            thickness = display.getThickness(OmniCrossbowAttachments.BEACON_LASER in entity, partialTickTime)
            xRot = entity.getXRot(partialTickTime)
            yRot = entity.getYRot(partialTickTime) - Mth.rotLerp(partialTickTime, entity.yBodyRotO, entity.yBodyRot)
        }

        companion object {
            @JvmStatic
            fun extractFrom(entity: LivingEntity, state: FabricRenderState, key: RenderStateDataKey<State>, partialTickTime: Float) {
                val display = entity[OmniCrossbowAttachments.BEACON_LASER_DISPLAY] ?: return
                state.getOrCreate(key, ::State).extractFrom(display, entity, partialTickTime)
            }
        }
    }

    class Layer<S : LivingEntityRenderState, M : EntityModel<S>>(renderer: RenderLayerParent<S, M>) : RenderLayer<S, M>(renderer) {
        override fun submit(
            poseStack: PoseStack,
            submitNodeCollector: SubmitNodeCollector,
            lightCoords: Int,
            state: S,
            yRot: Float,
            xRot: Float
        ) {
            val beaconState = state[BEACON_LASER] ?: return
            render(beaconState, poseStack, submitNodeCollector)
        }
    }
}
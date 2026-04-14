package archives.tater.omnicrossbow.client.render.entity

import archives.tater.omnicrossbow.entity.BeaconLaser
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.blockentity.BeaconRenderer
import net.minecraft.client.renderer.blockentity.BeaconRenderer.submitBeaconBeam
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.util.Mth.TWO_PI
import kotlin.math.sin

class BeaconLaserRenderer(context: EntityRendererProvider.Context) : EntityRenderer<BeaconLaser, BeaconLaserRenderer.State>(context) {

    override fun createRenderState(): State = State()

    override fun extractRenderState(entity: BeaconLaser, state: State, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        state.animationTime = entity.getAnimationTime(partialTicks)
        state.distance = entity.distance
        state.thickness = entity.getThickness(partialTicks)
        state.yRot = entity.getYRot(partialTicks)
        state.xRot = entity.getXRot(partialTicks)
    }

    override fun shouldRender(entity: BeaconLaser, culler: Frustum, camX: Double, camY: Double, camZ: Double): Boolean = true

    override fun submit(
        state: State,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        val thickness = state.thickness
        if (thickness <= 0) return

        poseStack.pushPose()

        poseStack.mulPose(Axis.YN.rotationDegrees(state.yRot))
        poseStack.mulPose(Axis.XP.rotationDegrees(state.xRot + 90))

        poseStack.translate(-0.5f, 0f, -0.5f)

        submitBeaconBeam(
            poseStack,
            submitNodeCollector,
            BeaconRenderer.BEAM_LOCATION,
            1f,
            BEAM_SPEED * state.animationTime,
            0,
            state.distance,
            0xFFFFFF,
            MAX_THICKNESS * thickness,
            (MAX_THICKNESS + 0.5f * PULSE_THICKNESS * (1 + sin(TWO_PI / PULSE_FREQUENCY * state.animationTime))) * thickness
        )

        poseStack.popPose()

        super.submit(state, poseStack, submitNodeCollector, camera)
    }

    class State : EntityRenderState() {
        @JvmField var animationTime: Float = 0f
        @JvmField var distance: Int = 0
        @JvmField var thickness: Float = 0f
        @JvmField var yRot: Float = 0f
        @JvmField var xRot: Float = 0f
    }

    companion object {
        const val MAX_THICKNESS = 0.25f
        const val BEAM_SPEED = 8f
        const val PULSE_THICKNESS = 0.1f
        const val PULSE_FREQUENCY = 4f
    }
}
package archives.tater.omnicrossbow.client.render.entity

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.Entity

abstract class BillboardEntityRenderer<T: Entity, S : EntityRenderState>(context: EntityRendererProvider.Context) : EntityRenderer<T, S>(context) {
    abstract val scale: Float
    abstract val renderType: RenderType

    override fun submit(
        state: S,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        poseStack.pushPose()
        val scale = scale
        poseStack.scale(scale, scale, scale)
        poseStack.mulPose(camera.orientation)
        poseStack.translate(0f, 0f, 0.25f)
        submitNodeCollector.submitCustomGeometry(poseStack, renderType) { pose, buffer ->
            vertex(buffer, pose, state.lightCoords, 0.0f, 0, 0, 1)
            vertex(buffer, pose, state.lightCoords, 1.0f, 0, 1, 1)
            vertex(buffer, pose, state.lightCoords, 1.0f, 1, 1, 0)
            vertex(buffer, pose, state.lightCoords, 0.0f, 1, 0, 0)
        }
        poseStack.popPose()
        super.submit(state, poseStack, submitNodeCollector, camera)
    }

    private fun vertex(
        builder: VertexConsumer, pose: PoseStack.Pose, lightCoords: Int, x: Float, y: Int, u: Int, v: Int
    ) {
        with (builder) {
            addVertex(pose, x - 0.5f, y - 0.25f, 0.0f)
            setColor(-1)
            setUv(u.toFloat(), v.toFloat())
            setOverlay(OverlayTexture.NO_OVERLAY)
            setLight(lightCoords)
            setNormal(pose, 0.0f, 1.0f, 0.0f)
        }
    }
}
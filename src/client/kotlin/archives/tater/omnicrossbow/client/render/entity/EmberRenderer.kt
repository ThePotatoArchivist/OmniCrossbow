package archives.tater.omnicrossbow.client.render.entity

import archives.tater.omnicrossbow.entity.Ember
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier

class EmberRenderer(context: EntityRendererProvider.Context) : EntityRenderer<Ember, EntityRenderState>(context) {
    override fun createRenderState(): EntityRenderState = EntityRenderState()

    override fun getBlockLightLevel(entity: Ember, blockPos: BlockPos): Int = 15

    override fun submit(
        state: EntityRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        poseStack.pushPose()
        poseStack.scale(0.25f, 0.25f, 0.25f)
        poseStack.mulPose(camera.orientation)
        poseStack.translate(0f, 0f, 0.25f)
        submitNodeCollector.submitCustomGeometry(
            poseStack,
            RENDER_TYPE
        ) { pose, buffer ->
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

    companion object {
        private val TEXTURE_ID: Identifier = Identifier.withDefaultNamespace("textures/particle/lava.png")
        private val RENDER_TYPE: RenderType = RenderTypes.entityCutout(TEXTURE_ID)
    }
}
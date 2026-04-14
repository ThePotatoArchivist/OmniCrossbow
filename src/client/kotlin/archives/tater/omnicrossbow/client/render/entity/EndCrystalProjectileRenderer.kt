package archives.tater.omnicrossbow.client.render.entity

import archives.tater.omnicrossbow.entity.EndCrystalProjectile
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.`object`.crystal.EndCrystalModel
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EndCrystalRenderState
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.core.BlockPos
import net.minecraft.resources.Identifier

class EndCrystalProjectileRenderer(context: EntityRendererProvider.Context) :
    EntityRenderer<EndCrystalProjectile, EndCrystalRenderState>(context) {

    private val model = Model(context.bakeLayer(ModelLayers.END_CRYSTAL));

    override fun getBlockLightLevel(entity: EndCrystalProjectile, blockPos: BlockPos): Int = 15

    override fun createRenderState() = EndCrystalRenderState()

    override fun extractRenderState(entity: EndCrystalProjectile, state: EndCrystalRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        state.showsBottom = false
    }

    override fun submit(
        state: EndCrystalRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        poseStack.pushPose()
        poseStack.scale(2.0f, 2.0f, 2.0f)
        poseStack.translate(0.0f, -1.25f, 0.0f)
        submitNodeCollector.submitModel(
            this.model,
            state,
            poseStack,
            RENDER_TYPE,
            state.lightCoords,
            OverlayTexture.NO_OVERLAY,
            state.outlineColor,
            null
        )
        poseStack.popPose()
        super.submit(state, poseStack, submitNodeCollector, camera)
    }

    override fun shouldRender(
        entity: EndCrystalProjectile,
        culler: Frustum,
        camX: Double,
        camY: Double,
        camZ: Double
    ): Boolean = true

    class Model(root: ModelPart) : EndCrystalModel(root)

    companion object {

        private val END_CRYSTAL_LOCATION: Identifier =
            Identifier.withDefaultNamespace("textures/entity/end_crystal/end_crystal.png")

        private val RENDER_TYPE: RenderType = RenderTypes.entityCutout(END_CRYSTAL_LOCATION)
    }
}
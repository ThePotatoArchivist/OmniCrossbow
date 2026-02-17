package archives.tater.omnicrossbow.client.render.entity

import archives.tater.omnicrossbow.entity.GrappleFishingHook
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.EntityRenderState
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.resources.Identifier

class GrappleFishingHookRenderer(context: EntityRendererProvider.Context) :
    BillboardEntityRenderer<GrappleFishingHook, EntityRenderState>(context) {

    override fun createRenderState(): EntityRenderState = EntityRenderState()

    override val scale: Float get() = 0.25f

    override val renderType: RenderType get() = RENDER_TYPE

    companion object {
        private val TEXTURE_ID: Identifier = Identifier.withDefaultNamespace("textures/entity/fishing/fishing_hook.png")
        private val RENDER_TYPE: RenderType = RenderTypes.entityCutout(TEXTURE_ID)
    }
}
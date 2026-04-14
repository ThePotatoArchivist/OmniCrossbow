package archives.tater.omnicrossbow.client.render.entity

import archives.tater.omnicrossbow.entity.GrappleFishingHook
import archives.tater.omnicrossbow.util.minus
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.SubmitNodeCollector
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.state.FishingHookRenderState
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.client.renderer.state.level.CameraRenderState
import net.minecraft.resources.Identifier
import net.minecraft.util.Mth.*
import net.minecraft.world.entity.HumanoidArm
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.phys.Vec3
import java.lang.Math.PI

class GrappleFishingHookRenderer(context: EntityRendererProvider.Context) :
    BillboardEntityRenderer<GrappleFishingHook, FishingHookRenderState>(context) {

    override fun createRenderState(): FishingHookRenderState = FishingHookRenderState()

    override val scale: Float get() = 0.25f

    override val renderType: RenderType get() = RENDER_TYPE

    override fun extractRenderState(entity: GrappleFishingHook, state: FishingHookRenderState, partialTicks: Float) {
        super.extractRenderState(entity, state, partialTicks)
        state.lineOriginOffset = (entity.owner as? LivingEntity)?.let {
            val swing = it.getAttackAnim(partialTicks)
            val swingAngle = sin(sqrt(swing) * PI)
            val playerPos = getHandPos(it, swingAngle, partialTicks)
            val hookPos = entity.getPosition(partialTicks).add(0.0, 0.25, 0.0)
            playerPos - hookPos
        } ?: Vec3.ZERO
    }

    override fun submit(
        state: FishingHookRenderState,
        poseStack: PoseStack,
        submitNodeCollector: SubmitNodeCollector,
        camera: CameraRenderState
    ) {
        val width = Minecraft.getInstance().window.appropriateLineWidth
        submitNodeCollector.submitCustomGeometry(poseStack, RenderTypes.lines()) { pose, buffer ->
            with(buffer) {
                addVertex(pose, 0f, 0f, 0f)
                setColor(0xFF000000u.toInt())
                setNormal(pose, 0f, 0.25f, 0f)
                setLineWidth(width)

                addVertex(pose, state.lineOriginOffset.toVector3f())
                setColor(0xFF000000u.toInt())
                setNormal(pose, 0f, 0.25f, 0f)
                setLineWidth(width)
            }
        }
        super.submit(state, poseStack, submitNodeCollector, camera)
    }

    private fun getHandPos(owner: LivingEntity, swing: Float, partialTicks: Float): Vec3 {
        val invert = if (getHoldingArm(owner) == HumanoidArm.RIGHT) 1 else -1
        if (entityRenderDispatcher.options.cameraType.isFirstPerson && owner === Minecraft.getInstance().player
        ) {
            val fov = entityRenderDispatcher.options.fov().get()
            val viewBobbingScale: Double = 960.0 / fov
            val viewVec: Vec3 = entityRenderDispatcher
                .camera!!
                .getNearPlane(fov.toFloat())
                .getPointOnPlane(invert * 0.525f, -0.1f)
                .scale(viewBobbingScale)
                .yRot(swing * 0.5f)
                .xRot(-swing * 0.7f)
            return owner.getEyePosition(partialTicks).add(viewVec)
        } else {
            val ownerYRot = lerp(partialTicks, owner.yBodyRotO, owner.yBodyRot) * DEG_TO_RAD
            val sin = sin(ownerYRot.toDouble()).toDouble()
            val cos = cos(ownerYRot.toDouble()).toDouble()
            val playerScale = owner.scale
            val rightOffset = invert * 0.35 * playerScale
            val forwardOffset = 0.8 * playerScale
            val yOffset = if (owner.isCrouching) -0.1875f else 0.0f
            return owner.getEyePosition(partialTicks)
                .add(
                    -cos * rightOffset - sin * forwardOffset,
                    yOffset - 0.45 * playerScale,
                    -sin * rightOffset + cos * forwardOffset
                )
        }
    }

    companion object {
        private val TEXTURE_ID: Identifier = Identifier.withDefaultNamespace("textures/entity/fishing/fishing_hook.png")
        private val RENDER_TYPE: RenderType = RenderTypes.entityCutout(TEXTURE_ID)

        fun getHoldingArm(owner: LivingEntity): HumanoidArm =
            if (owner.mainHandItem.item is CrossbowItem) owner.mainArm else owner.mainArm.opposite
    }
}
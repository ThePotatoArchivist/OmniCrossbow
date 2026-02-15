package archives.tater.omnicrossbow

import archives.tater.omnicrossbow.client.render.AmmoPosition
import archives.tater.omnicrossbow.client.render.ChargedProjectileIndicatorRenderer
import archives.tater.omnicrossbow.client.render.entity.BeaconLaserRenderer
import archives.tater.omnicrossbow.client.render.entity.EmberRenderer
import archives.tater.omnicrossbow.client.render.entity.EndCrystalProjectileRenderer
import archives.tater.omnicrossbow.client.render.item.OmniAmmoRenderer
import archives.tater.omnicrossbow.network.*
import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantmentEffects
import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import archives.tater.omnicrossbow.util.minus
import archives.tater.omnicrossbow.util.plus
import archives.tater.omnicrossbow.util.times
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey
import net.fabricmc.fabric.api.resource.v1.ResourceLoader
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.renderer.entity.EntityRenderers
import net.minecraft.client.renderer.entity.ThrownItemRenderer
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.packs.PackType
import net.minecraft.util.Mth.TWO_PI
import net.minecraft.world.entity.player.PlayerModelPart
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ChargedProjectiles
import net.minecraft.world.item.enchantment.EnchantmentHelper
import org.joml.Quaternionf

object OmniCrossbowClient : ClientModInitializer {

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
	fun shouldSpin(crossbow: ItemStack) = EnchantmentHelper.has(crossbow, OmniCrossbowEnchantmentEffects.CROSSBOW_SPIN)
			&& !crossbow.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).isEmpty

	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ChargedProjectileIndicatorRenderer.register()

		EntityRenderers.register(OmniCrossbowEntities.CUSTOM_ITEM_PROJECTILE, ::ThrownItemRenderer) // TODO temporary
		EntityRenderers.register(OmniCrossbowEntities.SLIME_BALL, ::ThrownItemRenderer)
		EntityRenderers.register(OmniCrossbowEntities.MAGMA_CREAM, ::ThrownItemRenderer)
		EntityRenderers.register(OmniCrossbowEntities.FREEZING_SNOWBALL, ::ThrownItemRenderer)
		EntityRenderers.register(OmniCrossbowEntities.END_CRYSTAL, ::EndCrystalProjectileRenderer)
		EntityRenderers.register(OmniCrossbowEntities.EMBER, ::EmberRenderer)
		EntityRenderers.register(OmniCrossbowEntities.BEACON_LASER, ::BeaconLaserRenderer)

		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(OmniCrossbow.id("ammo_position"), AmmoPosition)

		ModelLoadingPlugin.register { context ->
			context.modifyItemModelBeforeBake().register { model, context ->
				if (BuiltInRegistries.ITEM.getValue(context.itemId()) is CrossbowItem) {
					OmniAmmoRenderer.wrapModel(model)
				} else
					model
			}
		}

		ClientPlayNetworking.registerGlobalReceiver(HaircutPayload.TYPE) { _, context ->
			if (context.player() == context.client().player)
				context.client().options.setModelPart(PlayerModelPart.HAT, false)
		}

		ClientPlayNetworking.registerGlobalReceiver(FireworksPayload.TYPE) { (id), context ->
			val projectile = context.player().level().getEntity(id) as? ThrowableItemProjectile ?: return@registerGlobalReceiver
			val explosion = projectile.item[DataComponents.FIREWORK_EXPLOSION] ?: return@registerGlobalReceiver
			val movement = projectile.deltaMovement
			context.player().level().createFireworks(projectile.x, projectile.y, projectile.z, movement.x, movement.y, movement.z, listOf(explosion))
		}

		ClientPlayNetworking.registerGlobalReceiver(ParticleBeamPayload.TYPE) { (particle, start, end, step, randomness, countPerPos, dx, dy, dz, speed), context ->
			val difference = end - start
			val direction = difference.normalize()
			val random = context.player().random
			repeat((difference.length() / step).toInt()) {
				val particlePos = start + direction * ((it + randomness * random.nextDouble()) * step)
				repeat(countPerPos) {
					context.player().level().addParticle(
                        particle,
						particlePos.x + random.nextGaussian() * dx,
						particlePos.y + random.nextGaussian() * dy,
						particlePos.z + random.nextGaussian() * dz,
						random.nextGaussian() * speed,
						random.nextGaussian() * speed,
						random.nextGaussian() * speed
					)
				}
			}
		}

		ClientPlayNetworking.registerGlobalReceiver(AddMovementPayload.TYPE) { (movement, resetFalling), context ->
			context.player().addMovementClient(movement, resetFalling)
		}
	}
}
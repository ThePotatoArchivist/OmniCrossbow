package archives.tater.omnicrossbow

import archives.tater.omnicrossbow.client.render.AmmoPosition
import archives.tater.omnicrossbow.client.render.ChargedProjectileIndicatorRenderer
import archives.tater.omnicrossbow.client.render.entity.BeaconLaserRenderer
import archives.tater.omnicrossbow.client.render.entity.EmberRenderer
import archives.tater.omnicrossbow.client.render.entity.EndCrystalProjectileRenderer
import archives.tater.omnicrossbow.client.render.entity.GrappleFishingHookRenderer
import archives.tater.omnicrossbow.client.render.item.OmniAmmoRenderer
import archives.tater.omnicrossbow.entity.SpyEnderEye
import archives.tater.omnicrossbow.network.*
import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import archives.tater.omnicrossbow.util.minus
import archives.tater.omnicrossbow.util.plus
import archives.tater.omnicrossbow.util.times
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.resource.v1.ResourceLoader
import net.minecraft.client.CameraType
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.entity.EntityRenderers
import net.minecraft.client.renderer.entity.ThrownItemRenderer
import net.minecraft.client.renderer.special.SpecialModelRenderers
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.chat.Component
import net.minecraft.resources.Identifier
import net.minecraft.server.packs.PackType
import net.minecraft.world.entity.player.PlayerModelPart
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.CrossbowItem
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3

object OmniCrossbowClient : ClientModInitializer {

	@JvmField
    var spyEye: SpyEnderEye? = null
	var lastEyeInput: Vec3 = Vec3.ZERO
	const val EYE_HINT = "omnicrossbow.endereye.beginview"

	@JvmStatic
	fun isCrossbow(id: Identifier?): Boolean = BuiltInRegistries.ITEM.getValue(id) is CrossbowItem

	@JvmStatic
	fun renderEyeVignette(graphics: GuiGraphics, location: Identifier) {
		if (spyEye == null || Minecraft.getInstance().options.cameraType != CameraType.FIRST_PERSON) return

		graphics.blit(RenderPipelines.GUI_NAUSEA_OVERLAY, location, 0, 0, 0.0F, 0.0F, graphics.guiWidth(), graphics.guiHeight(), graphics.guiWidth(), graphics.guiHeight(), 0xFF71AC49u.toInt());
	}

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
		EntityRenderers.register(OmniCrossbowEntities.SPY_ENDER_EYE, ::ThrownItemRenderer)
		EntityRenderers.register(OmniCrossbowEntities.GRAPPLE_FISHING_HOOK, ::GrappleFishingHookRenderer)
		EntityRenderers.register(OmniCrossbowEntities.CROSSBOW, ::ThrownItemRenderer)

		ResourceLoader.get(PackType.CLIENT_RESOURCES).registerReloadListener(OmniCrossbow.id("ammo_position"), AmmoPosition)

		SpecialModelRenderers.ID_MAPPER.put(OmniCrossbow.id("crossbow_ammo"), OmniAmmoRenderer.Unbaked.CODEC)

		ModelLoadingPlugin.register { context ->
			context.modifyItemModelBeforeBake().register { model, context ->
				if (isCrossbow(context.itemId())) {
					OmniAmmoRenderer.wrapModel(model)
				} else
					model
			}
		}

		ClientTickEvents.START_CLIENT_TICK.register { minecraft ->
			val spyEye = spyEye ?: return@register
			if (spyEye.isRemoved || spyEye.level() != minecraft.level) {
				this.spyEye = null
				lastEyeInput = Vec3.ZERO
				return@register
			}
			val input = if (minecraft.options.keyUp.isDown) {
				spyEye.lookAngle
			} else {
				Vec3.ZERO
			}
			if (input != lastEyeInput) {
				ClientPlayNetworking.send(SpyEyeInputPayload(input))
				lastEyeInput = input
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

		ClientPlayNetworking.registerGlobalReceiver(ViewSpyEyePayload.TYPE) { (entityId), context ->
			spyEye = context.client().level!!.getEntity(entityId) as? SpyEnderEye
			context.player().displayClientMessage(Component.translatable(EYE_HINT, context.client().options.keyShift.translatedKeyMessage), true)
		}
	}
}
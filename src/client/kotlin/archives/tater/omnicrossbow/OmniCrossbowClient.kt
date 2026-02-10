package archives.tater.omnicrossbow

import archives.tater.omnicrossbow.client.render.AmmoPosition
import archives.tater.omnicrossbow.client.render.ChargedProjectileIndicatorRenderer
import archives.tater.omnicrossbow.client.render.item.OmniAmmoRenderer
import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.network.FireworksPayload
import archives.tater.omnicrossbow.network.HaircutPayload
import archives.tater.omnicrossbow.network.ParticleBeamPayload
import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import archives.tater.omnicrossbow.util.minus
import archives.tater.omnicrossbow.util.plus
import archives.tater.omnicrossbow.util.times
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.resource.v1.ResourceLoader
import net.minecraft.client.renderer.entity.EntityRenderers
import net.minecraft.client.renderer.entity.ThrownItemRenderer
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.packs.PackType
import net.minecraft.world.entity.player.PlayerModelPart
import net.minecraft.world.item.CrossbowItem

object OmniCrossbowClient : ClientModInitializer {

	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ChargedProjectileIndicatorRenderer.register()

		EntityRenderers.register(OmniCrossbowEntities.CUSTOM_ITEM_PROJECTILE, ::ThrownItemRenderer) // TODO temporary

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
			val projectile = context.player().level().getEntity(id) as? CustomItemProjectile ?: return@registerGlobalReceiver
			val explosion = projectile.item[DataComponents.FIREWORK_EXPLOSION] ?: return@registerGlobalReceiver
			val movement = projectile.deltaMovement
			context.player().level().createFireworks(projectile.x, projectile.y, projectile.z, movement.x, movement.y, movement.z, listOf(explosion))
		}

		ClientPlayNetworking.registerGlobalReceiver(ParticleBeamPayload.TYPE) { payload, context ->
			val difference = payload.end - payload.start
			val direction = difference.normalize()
			val random = context.player().random
			repeat((difference.length() / payload.step).toInt()) {
				val particlePos = payload.start + direction * ((it + random.nextDouble()) * payload.step)
				repeat(payload.countPerPos) {
					context.player().level().addParticle(
						payload.particle,
						particlePos.x + random.nextGaussian() * payload.dx,
						particlePos.y + random.nextGaussian() * payload.dy,
						particlePos.z + random.nextGaussian() * payload.dz,
						random.nextGaussian() * payload.speed,
						random.nextGaussian() * payload.speed,
						random.nextGaussian() * payload.speed
					)
				}
			}
		}
	}
}
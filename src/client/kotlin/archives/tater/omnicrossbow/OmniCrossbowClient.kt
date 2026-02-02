package archives.tater.omnicrossbow

import archives.tater.omnicrossbow.client.render.AmmoPosition
import archives.tater.omnicrossbow.client.render.ChargedProjectileIndicatorRenderer
import archives.tater.omnicrossbow.client.render.item.OmniAmmoRenderer
import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.fabricmc.fabric.api.resource.v1.ResourceLoader
import net.minecraft.client.renderer.entity.EntityRenderers
import net.minecraft.client.renderer.entity.ThrownItemRenderer
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.packs.PackType
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
	}
}
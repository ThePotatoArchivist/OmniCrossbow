package archives.tater.omnicrossbow

import archives.tater.omnicrossbow.client.render.ChargedProjectileIndicatorRenderer
import archives.tater.omnicrossbow.client.render.item.OmniAmmoRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.CrossbowItem

object OmniCrossbowClient : ClientModInitializer {

	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ChargedProjectileIndicatorRenderer.register()

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
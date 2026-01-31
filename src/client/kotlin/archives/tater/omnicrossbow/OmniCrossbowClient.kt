package archives.tater.omnicrossbow

import archives.tater.omnicrossbow.client.render.ChargedProjectileIndicatorRenderer
import net.fabricmc.api.ClientModInitializer

object OmniCrossbowClient : ClientModInitializer {
	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ChargedProjectileIndicatorRenderer.register()
	}
}
package archives.tater.omnicrossbow

import archives.tater.omnicrossbow.client.render.ChargedProjectileIndicatorRenderer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry

object OmniCrossbowClient : ClientModInitializer {
	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		HudElementRegistry.addLast(ChargedProjectileIndicatorRenderer.ID, ChargedProjectileIndicatorRenderer)
	}
}
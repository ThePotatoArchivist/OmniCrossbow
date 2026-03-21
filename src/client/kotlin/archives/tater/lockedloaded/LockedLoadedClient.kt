package archives.tater.lockedloaded

import archives.tater.lockedloaded.client.render.ChargedProjectileIndicatorRenderer
import net.fabricmc.api.ClientModInitializer

object LockedLoadedClient : ClientModInitializer {

	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		ChargedProjectileIndicatorRenderer.register()
	}
}
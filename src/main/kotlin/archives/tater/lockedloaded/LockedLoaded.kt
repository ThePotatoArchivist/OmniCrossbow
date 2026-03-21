package archives.tater.lockedloaded

import archives.tater.lockedloaded.registry.*
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

object LockedLoaded : ModInitializer {
	const val MOD_ID = "lockedloaded"

	fun id(path: String): Identifier = Identifier.fromNamespaceAndPath(MOD_ID, path)

    val logger: Logger = LogManager.getLogger(MOD_ID)

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		LockedLoadedEnchantmentEffects.init()
		LockedLoadedAttachments.init()
		LockedLoadedComponents.init()
		LockedLoadedConditions.init()
		LockedLoadedSounds.init()
	}
}
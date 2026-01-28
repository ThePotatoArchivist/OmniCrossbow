package archives.tater.omnicrossbow

import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantmentEffects
import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier
import org.apache.logging.log4j.LogManager

object OmniCrossbow : ModInitializer {
	const val MOD_ID = "omnicrossbow"

	fun id(path: String): Identifier = Identifier.fromNamespaceAndPath(MOD_ID, path)

    private val logger = LogManager.getLogger(MOD_ID)

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		OmniCrossbowEnchantmentEffects.init()
	}
}
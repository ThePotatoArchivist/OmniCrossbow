package archives.tater.omnicrossbow.registry

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

object OmniCrossbowBuiltinRegistries {
    private fun <T: Any> register(key: ResourceKey<Registry<T>>, init: FabricRegistryBuilder<T, MappedRegistry<T>>.() -> Unit = {}): Registry<T> =
        FabricRegistryBuilder.create(key).apply(init).buildAndRegister()

    @JvmField val PROJECTILE_ACTION_TYPE = register(OmniCrossbowRegistries.PROJECTILE_ACTION_TYPE)
    @JvmField val BLOCK_IMPACT_ACTION = register(OmniCrossbowRegistries.BLOCK_IMPACT_ACTION)
    @JvmField val BLOCK_IMPACT_ACTION_TYPE = register(OmniCrossbowRegistries.BLOCK_IMPACT_ACTION_TYPE)
    @JvmField val ENTITY_IMPACT_ACTION = register(OmniCrossbowRegistries.ENTITY_IMPACT_ACTION)
    @JvmField val ENTITY_IMPACT_ACTION_TYPE = register(OmniCrossbowRegistries.ENTITY_IMPACT_ACTION_TYPE)

    fun init() {

    }
}
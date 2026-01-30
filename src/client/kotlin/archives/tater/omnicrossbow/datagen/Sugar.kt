package archives.tater.omnicrossbow.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack.RegistryDependentFactory
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.data.tags.TagAppender
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey

context(appender: TagAppender<E, *>)
operator fun <E: Any> E.unaryPlus() {
    appender.add(this)
}

context(appender: TagAppender<*, T>)
operator fun <T: Any> TagKey<T>.unaryPlus() {
    appender.addTag(this)
}

fun <T: Any> dynamicRegistry(name: String, registry: ResourceKey<Registry<T>>): RegistryDependentFactory<FabricDynamicRegistryProvider> = { output, registriesFuture ->
    object : FabricDynamicRegistryProvider(output, registriesFuture) {
        override fun configure(registries: HolderLookup.Provider, entries: Entries) {
            entries.addAll(registries.lookupOrThrow(registry))
        }

        override fun getName(): String = name
    }
}
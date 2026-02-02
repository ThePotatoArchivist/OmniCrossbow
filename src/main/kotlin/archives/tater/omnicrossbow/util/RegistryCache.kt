package archives.tater.omnicrossbow.util

import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

abstract class RegistryCache<T> {
    private val cache: WeakHashMap<HolderLookup.Provider, T> = WeakHashMap()

    abstract fun populate(registries: HolderLookup.Provider): T

    operator fun get(registries: HolderLookup.Provider): T = cache.getOrPut(registries) { populate(registries) }

    class Direct<T: Any, K, V>(
        private val registryRef: ResourceKey<Registry<T>>,
        private val getKey: (Holder<T>) -> K,
        private val getValue: (Holder<T>) -> V,
    ) : RegistryCache<Map<K, V>>() {
        override fun populate(registries: HolderLookup.Provider): Map<K, V> =
            registries.lookupOrThrow(registryRef).listElements()
                .collect(Collectors.toMap(getKey) { getValue(it) })

        companion object {
            inline fun <T: Any, K, V> values(
                registryRef: ResourceKey<Registry<T>>,
                crossinline getKey: (T) -> K,
                crossinline getValue: (T) -> V,
            ) = Direct(registryRef, { getKey(it.value()) }, { getValue(it.value()) })
        }
    }

    class Contextual<T: Any, K, V>(
        private val registryRef: ResourceKey<Registry<T>>,
        private val getKey: (Holder<T>) -> K,
        private val getValue: (Holder<T>, HolderLookup.Provider) -> V,
    ) : RegistryCache<Map<K, V>>() {
        override fun populate(registries: HolderLookup.Provider): Map<K, V> =
            registries.lookupOrThrow(registryRef).listElements()
                .collect(Collectors.toMap(getKey) { getValue(it, registries) })

        companion object {
            inline fun <T: Any, K, V> values(
                registryRef: ResourceKey<Registry<T>>,
                crossinline getKey: (T) -> K,
                crossinline getValue: (T, HolderLookup.Provider) -> V,
            ) = Contextual(registryRef, { getKey(it.value()) }, { holder, registries -> getValue(holder.value(), registries) })

            fun <T: Any, U: Any> idMatching(
                registryRef: ResourceKey<Registry<T>>,
                valuesRegistry: ResourceKey<Registry<U>>,
                fallback: (T) -> U
            ) = Contextual<T, T, U>(registryRef, { it.value() }, { holder, registries ->
                registries.lookupOrThrow(valuesRegistry)
                    .get(ResourceKey.create(valuesRegistry, holder.unwrapKey().orElseThrow().identifier()))
                    .map { it.value() }
                    .orElseGet { fallback(holder.value()) }
            })
        }
    }

    class BackRef<T: Any, K, V>(
        private val registryRef: ResourceKey<Registry<T>>,
        private val getValue: (Holder<T>) -> V,
        private val getKeys: (Holder<T>) -> Stream<K>
    ) : RegistryCache<Map<K, V>>() {
        override fun populate(registries: HolderLookup.Provider): Map<K, V> = buildMap {
            for (holder in registries.lookupOrThrow(registryRef).listElements()) {
                val value = getValue(holder)
                for (key in getKeys(holder))
                    this[key] = value
            }
        }

        companion object {
            inline fun <T: Any, K, V> values(
                registryRef: ResourceKey<Registry<T>>,
                crossinline getValue: (T) -> V,
                crossinline getKeys: (T) -> Stream<K>
            ) = BackRef(registryRef, { getValue(it.value()) }, { getKeys(it.value()) })
        }
    }
}
package archives.tater.lockedloaded.registry

import archives.tater.lockedloaded.LockedLoaded
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey

object LockedLoadedTags {
    fun <T: Any> of(registry: ResourceKey<Registry<T>>, path: String): TagKey<T> = TagKey.create(registry, LockedLoaded.id(path))
    fun ofItem(path: String) = of(Registries.ITEM, path)
    fun ofBlock(path: String) = of(Registries.BLOCK, path)
    fun ofEntity(path: String) = of(Registries.ENTITY_TYPE, path)



}
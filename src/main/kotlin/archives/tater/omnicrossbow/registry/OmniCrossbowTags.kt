package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey

object OmniCrossbowTags {
    fun <T: Any> of(registry: ResourceKey<Registry<T>>, path: String): TagKey<T> = TagKey.create(registry, OmniCrossbow.id(path))
    fun ofItem(path: String) = of(Registries.ITEM, path)
    fun ofBlock(path: String) = of(Registries.BLOCK, path)
    fun ofEntity(path: String) = of(Registries.ENTITY_TYPE, path)


    @JvmField val BUILTIN_PROJECTILES = ofItem("builtin_projectiles")

    @JvmField val HAS_PREFERRED_TOOL = ofBlock("has_preferred_tool")

    @JvmField val CAN_ALWAYS_EQUIP = ofEntity("can_always_equip")

    @JvmField val EXTRA_BEACON_DAMAGE = ofEntity("extra_beacon_damage")

    @JvmField val UNCAPPED_BEACON_DAMAGE = ofEntity("uncapped_beacon_damage")
}
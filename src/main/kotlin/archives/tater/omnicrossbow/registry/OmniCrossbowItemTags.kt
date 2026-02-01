package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item

object OmniCrossbowItemTags {
    fun of(path: String): TagKey<Item> = TagKey.create(Registries.ITEM, OmniCrossbow.id(path))

    @JvmField
    val BUILTIN_PROJECTILES = of("builtin_projectiles")
}
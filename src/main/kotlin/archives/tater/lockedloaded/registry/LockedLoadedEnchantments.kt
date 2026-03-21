package archives.tater.lockedloaded.registry

import archives.tater.lockedloaded.LockedLoaded
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.item.enchantment.Enchantment

object LockedLoadedEnchantments {
    fun of(path: String): ResourceKey<Enchantment> = ResourceKey.create(Registries.ENCHANTMENT, LockedLoaded.id(path))

    @JvmField val MULTICHAMBERED = of("multichambered")
    @JvmField val PUMP_CHARGE = of("pump_charge")
    @JvmField val MAGAZINE = of("magazine")
    @JvmField val SHARPSHOOTING = of("sharpshooting")
    @JvmField val TWIRLING_CURSE = of("twirling_curse")

    @JvmField val MAGAZINE_EXCLUSIVE: TagKey<Enchantment> = TagKey.create(Registries.ENCHANTMENT, LockedLoaded.id("exclusive_set/magazine"))
    @JvmField val MULTICHAMBERED_EXCLUSIVE: TagKey<Enchantment> = TagKey.create(Registries.ENCHANTMENT, LockedLoaded.id("exclusive_set/multichambered"))
    @JvmField val PUMP_CHARGE_EXCLUSIVE: TagKey<Enchantment> = TagKey.create(Registries.ENCHANTMENT, LockedLoaded.id("exclusive_set/pump_charge"))
}
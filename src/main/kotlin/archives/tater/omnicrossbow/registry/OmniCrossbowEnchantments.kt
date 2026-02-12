package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.TagKey
import net.minecraft.world.item.enchantment.Enchantment

object OmniCrossbowEnchantments {
    fun of(path: String): ResourceKey<Enchantment> = ResourceKey.create(Registries.ENCHANTMENT, OmniCrossbow.id(path))

    @JvmField val MULTICHAMBERED = of("multichambered")
    @JvmField val PUMP_CHARGE = of("pump_charge")
    @JvmField val MAGAZINE = of("magazine")
    @JvmField val OMNI = of("omni")
    @JvmField val SHARPSHOOTING = of("sharpshooting")
    @JvmField val TWIRLING_CURSE = of("twirling_curse")

    @JvmField val MULTICHAMBERED_EXCLUSIVE: TagKey<Enchantment> = TagKey.create(Registries.ENCHANTMENT, OmniCrossbow.id("exclusive_set/multichambered"))
}
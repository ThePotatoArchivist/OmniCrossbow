package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.providers.EnchantmentProvider

object OmniCrossbowEnchantments {
    fun of(path: String): ResourceKey<Enchantment> = ResourceKey.create(Registries.ENCHANTMENT, OmniCrossbow.id(path))
    fun providerOf(path: String): ResourceKey<EnchantmentProvider> = ResourceKey.create(Registries.ENCHANTMENT_PROVIDER, OmniCrossbow.id(path))

    @JvmField val OMNI = of("omni")

    @JvmField val RAID_PILLAGER_FINAL_WAVE_UNIQUE = providerOf("raid_pillager_final_wave_unique")
}
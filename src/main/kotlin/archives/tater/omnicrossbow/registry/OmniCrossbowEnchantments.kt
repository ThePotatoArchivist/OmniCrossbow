package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.enchantment.Enchantment

object OmniCrossbowEnchantments {
    fun of(path: String): ResourceKey<Enchantment> = ResourceKey.create(Registries.ENCHANTMENT, OmniCrossbow.id(path))

    val MULTICHAMBERED = of("multichambered")

}
package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.enchantment.Ammo
import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantmentEffects
import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantments
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.EnchantmentTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantment.*

object EnchantmentGenerator : RegistrySetBuilder.RegistryBootstrap<Enchantment> {
    override fun run(registry: BootstrapContext<Enchantment>) {
        val items = registry.lookup(Registries.ITEM)
        val enchantments = registry.lookup(Registries.ENCHANTMENT)
        val crossbowEnchantable = items.getOrThrow(ItemTags.CROSSBOW_ENCHANTABLE)

        fun register(key: ResourceKey<Enchantment>, definition: EnchantmentDefinition, init: Enchantment.Builder.() -> Unit) =
            enchantment(definition).apply(init).build(key.identifier()).also {
                registry[key] = it
            }

        register(OmniCrossbowEnchantments.OMNI, definition(
            crossbowEnchantable,
            1,
            1,
            constantCost(20),
            constantCost(50),
            8,
            EquipmentSlotGroup.MAINHAND
        )) {
            exclusiveWith(enchantments.getOrThrow(EnchantmentTags.CROSSBOW_EXCLUSIVE))
            withSpecialEffect(OmniCrossbowEnchantmentEffects.AMMO, listOf(Ammo.anyItem()))
        }
    }
}
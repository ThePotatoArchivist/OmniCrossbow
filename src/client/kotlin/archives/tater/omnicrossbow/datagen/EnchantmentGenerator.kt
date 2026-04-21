package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantmentEffects
import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantments
import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import archives.tater.omnicrossbow.util.*
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.EnchantmentTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantment.*
import net.minecraft.world.level.storage.loot.LootContext.EntityTarget
import net.minecraft.world.level.storage.loot.functions.SetComponentsFunction.setComponent
import net.minecraft.world.level.storage.loot.predicates.InvertedLootItemCondition.invert
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition.hasProperties

object EnchantmentGenerator : RegistrySetBuilder.RegistryBootstrap<Enchantment> {
    override fun run(registry: BootstrapContext<Enchantment>) {
        val items = registry.lookup(Registries.ITEM)
        val enchantments = registry.lookup(Registries.ENCHANTMENT)
        val entities = registry.lookup(Registries.ENTITY_TYPE)
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
            withEffect(OmniCrossbowEnchantmentEffects.ALLOW_ANY_PROJECTILE, McUnit.INSTANCE)
            withEffect(OmniCrossbowEnchantmentEffects.DEFAULT_PROJECTILE, LootTable {
                pool {
                    tag(OmniCrossbowTags.MOB_RANDOM_AMMO)
                    apply(setComponent(DataComponents.INTANGIBLE_PROJECTILE, McUnit.INSTANCE))
                }
            }, invert(hasProperties(EntityTarget.THIS, EntityPredicate {
                of(entities, EntityType.PLAYER)
            })))
        }
    }
}
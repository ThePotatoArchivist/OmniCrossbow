package archives.tater.omnicrossbow.datagen

import archives.tater.omnicrossbow.enchantment.ChargedProjectileIndicator
import archives.tater.omnicrossbow.enchantment.LoadMultiple
import archives.tater.omnicrossbow.enchantment.ProjectileUncertainty
import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantmentEffects
import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantments
import archives.tater.omnicrossbow.util.McUnit
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.EnchantmentTags
import net.minecraft.tags.ItemTags
import net.minecraft.world.entity.EquipmentSlotGroup
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantment.*
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents
import net.minecraft.world.item.enchantment.LevelBasedValue
import net.minecraft.world.item.enchantment.effects.AddValue
import net.minecraft.world.item.enchantment.effects.MultiplyValue
import net.minecraft.world.item.enchantment.effects.SetValue

object EnchantmentGenerator : RegistrySetBuilder.RegistryBootstrap<Enchantment> {
    override fun run(registry: BootstrapContext<Enchantment>) {
        val items = registry.lookup(Registries.ITEM)
        val enchantments = registry.lookup(Registries.ENCHANTMENT)
        val crossbowEnchantable = items.getOrThrow(ItemTags.CROSSBOW_ENCHANTABLE)

        fun register(key: ResourceKey<Enchantment>, definition: EnchantmentDefinition, init: Enchantment.Builder.() -> Unit) =
            enchantment(definition).apply(init).build(key.identifier()).also {
                registry[key] = it
            }

        register(OmniCrossbowEnchantments.MULTICHAMBERED, definition(
            crossbowEnchantable,
            4,
            3,
            dynamicCost(12, 20),
            constantCost(50),
            2,
            EquipmentSlotGroup.MAINHAND
        )) {
            exclusiveWith(enchantments.getOrThrow(OmniCrossbowEnchantments.MULTICHAMBERED_EXCLUSIVE))
            withSpecialEffect(OmniCrossbowEnchantmentEffects.LOAD_MULTIPLE, LoadMultiple(LevelBasedValue.perLevel(2f)))
            withSpecialEffect(OmniCrossbowEnchantmentEffects.CHARGED_PROJECTILE_INDICATOR, ChargedProjectileIndicator(LevelBasedValue.perLevel(2f)))
            withEffect(OmniCrossbowEnchantmentEffects.PROJECTILE_FIRED_COUNT, SetValue(LevelBasedValue.constant(1f)))
        }

        register(OmniCrossbowEnchantments.PUMP_CHARGE, definition(
            crossbowEnchantable,
            1,
            1,
            constantCost(20),
            constantCost(50),
            8,
            EquipmentSlotGroup.MAINHAND
        )) {
            exclusiveWith(enchantments.getOrThrow(OmniCrossbowEnchantments.PUMP_CHARGE_EXCLUSIVE))
            withSpecialEffect(OmniCrossbowEnchantmentEffects.LOAD_MULTIPLE, LoadMultiple(LevelBasedValue.constant(8f)))
            withSpecialEffect(OmniCrossbowEnchantmentEffects.CHARGED_PROJECTILE_INDICATOR, ChargedProjectileIndicator(LevelBasedValue.constant(8f)))
            withEffect(OmniCrossbowEnchantmentEffects.PROJECTILE_UNCERTAINTY, ProjectileUncertainty(projectileCount = AddValue(LevelBasedValue.perLevel(2f))))
            withEffect(OmniCrossbowEnchantmentEffects.PROJECTILE_VELOCITY, MultiplyValue(LevelBasedValue.constant(0.5f)))
            withEffect(EnchantmentEffectComponents.PROJECTILE_COUNT, SetValue(LevelBasedValue.constant(2f)))
        }

        register(OmniCrossbowEnchantments.MAGAZINE, definition(
            crossbowEnchantable,
            1,
            4,
            dynamicCost(12, 20),
            constantCost(50),
            8,
            EquipmentSlotGroup.MAINHAND
        )) {
            exclusiveWith(enchantments.getOrThrow(OmniCrossbowEnchantments.MAGAZINE_EXCLUSIVE))
            withEffect(EnchantmentEffectComponents.PROJECTILE_COUNT, SetValue(LevelBasedValue.perLevel(4f)))
            withSpecialEffect(OmniCrossbowEnchantmentEffects.CHARGED_PROJECTILE_INDICATOR, ChargedProjectileIndicator(LevelBasedValue.perLevel(4f)))
            withEffect(OmniCrossbowEnchantmentEffects.PROJECTILE_FIRED_COUNT, SetValue(LevelBasedValue.constant(1f)))
            withEffect(OmniCrossbowEnchantmentEffects.PROJECTILE_UNCERTAINTY, ProjectileUncertainty(projectileCount = AddValue(LevelBasedValue.perLevel(0.5f))))
            withEffect(OmniCrossbowEnchantmentEffects.CROSSBOW_COOLDOWN, AddValue(LevelBasedValue.constant(0.5f)))
            withSpecialEffect(EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME, AddValue(LevelBasedValue.perLevel(2f)))
        }

        register(OmniCrossbowEnchantments.SHARPSHOOTING, definition(
            crossbowEnchantable,
            1,
            3,
            dynamicCost(12, 20),
            constantCost(50),
            8,
            EquipmentSlotGroup.MAINHAND
        )) {
            exclusiveWith(enchantments.getOrThrow(EnchantmentTags.CROSSBOW_EXCLUSIVE))
            withEffect(OmniCrossbowEnchantmentEffects.PROJECTILE_RICOCHET, AddValue(LevelBasedValue.perLevel(1f)))
            withEffect(OmniCrossbowEnchantmentEffects.PROJECTILE_IGNORE_OWNER, McUnit.INSTANCE)
            withEffect(EnchantmentEffectComponents.PROJECTILE_PIERCING, AddValue(LevelBasedValue.perLevel(1f)))
        }

        register(OmniCrossbowEnchantments.TWIRLING_CURSE, definition(
            crossbowEnchantable,
            1,
            1,
            constantCost(25),
            constantCost(50),
            8,
            EquipmentSlotGroup.MAINHAND
        )) {
            withSpecialEffect(OmniCrossbowEnchantmentEffects.CROSSBOW_SPIN, LevelBasedValue.constant(20f))
        }
    }
}
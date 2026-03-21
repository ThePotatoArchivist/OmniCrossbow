package archives.tater.lockedloaded.datagen

import archives.tater.lockedloaded.enchantment.ChargedProjectileIndicator
import archives.tater.lockedloaded.enchantment.LoadMultiple
import archives.tater.lockedloaded.enchantment.ProjectileUncertainty
import archives.tater.lockedloaded.registry.LockedLoadedEnchantmentEffects
import archives.tater.lockedloaded.registry.LockedLoadedEnchantments
import archives.tater.lockedloaded.util.McUnit
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

        register(LockedLoadedEnchantments.MULTICHAMBERED, definition(
            crossbowEnchantable,
            4,
            3,
            dynamicCost(12, 20),
            constantCost(50),
            2,
            EquipmentSlotGroup.MAINHAND
        )) {
            exclusiveWith(enchantments.getOrThrow(LockedLoadedEnchantments.MULTICHAMBERED_EXCLUSIVE))
            withSpecialEffect(LockedLoadedEnchantmentEffects.LOAD_MULTIPLE, LoadMultiple(LevelBasedValue.perLevel(2f)))
            withSpecialEffect(LockedLoadedEnchantmentEffects.CHARGED_PROJECTILE_INDICATOR, ChargedProjectileIndicator(LevelBasedValue.perLevel(2f)))
            withEffect(LockedLoadedEnchantmentEffects.PROJECTILE_FIRED_COUNT, SetValue(LevelBasedValue.constant(1f)))
        }

        register(LockedLoadedEnchantments.PUMP_CHARGE, definition(
            crossbowEnchantable,
            1,
            1,
            constantCost(20),
            constantCost(50),
            8,
            EquipmentSlotGroup.MAINHAND
        )) {
            exclusiveWith(enchantments.getOrThrow(LockedLoadedEnchantments.PUMP_CHARGE_EXCLUSIVE))
            withSpecialEffect(LockedLoadedEnchantmentEffects.LOAD_MULTIPLE, LoadMultiple(LevelBasedValue.constant(8f)))
            withSpecialEffect(LockedLoadedEnchantmentEffects.CHARGED_PROJECTILE_INDICATOR, ChargedProjectileIndicator(LevelBasedValue.constant(8f)))
            withEffect(LockedLoadedEnchantmentEffects.PROJECTILE_UNCERTAINTY, ProjectileUncertainty(projectileCount = AddValue(LevelBasedValue.perLevel(2f))))
            withEffect(LockedLoadedEnchantmentEffects.PROJECTILE_VELOCITY, MultiplyValue(LevelBasedValue.constant(0.5f)))
            withEffect(EnchantmentEffectComponents.PROJECTILE_COUNT, SetValue(LevelBasedValue.constant(2f)))
        }

        register(LockedLoadedEnchantments.MAGAZINE, definition(
            crossbowEnchantable,
            1,
            4,
            dynamicCost(12, 20),
            constantCost(50),
            8,
            EquipmentSlotGroup.MAINHAND
        )) {
            exclusiveWith(enchantments.getOrThrow(LockedLoadedEnchantments.MAGAZINE_EXCLUSIVE))
            withEffect(EnchantmentEffectComponents.PROJECTILE_COUNT, SetValue(LevelBasedValue.perLevel(4f)))
            withSpecialEffect(LockedLoadedEnchantmentEffects.CHARGED_PROJECTILE_INDICATOR, ChargedProjectileIndicator(LevelBasedValue.perLevel(4f)))
            withEffect(LockedLoadedEnchantmentEffects.PROJECTILE_FIRED_COUNT, SetValue(LevelBasedValue.constant(1f)))
            withEffect(LockedLoadedEnchantmentEffects.PROJECTILE_UNCERTAINTY, ProjectileUncertainty(projectileCount = AddValue(LevelBasedValue.perLevel(0.5f))))
            withEffect(LockedLoadedEnchantmentEffects.CROSSBOW_COOLDOWN, AddValue(LevelBasedValue.constant(0.5f)))
            withSpecialEffect(EnchantmentEffectComponents.CROSSBOW_CHARGE_TIME, AddValue(LevelBasedValue.perLevel(2f)))
        }

        register(LockedLoadedEnchantments.SHARPSHOOTING, definition(
            crossbowEnchantable,
            1,
            3,
            dynamicCost(12, 20),
            constantCost(50),
            8,
            EquipmentSlotGroup.MAINHAND
        )) {
            exclusiveWith(enchantments.getOrThrow(EnchantmentTags.CROSSBOW_EXCLUSIVE))
            withEffect(LockedLoadedEnchantmentEffects.PROJECTILE_RICOCHET, AddValue(LevelBasedValue.perLevel(1f)))
            withEffect(LockedLoadedEnchantmentEffects.PROJECTILE_IGNORE_OWNER, McUnit.INSTANCE)
            withEffect(EnchantmentEffectComponents.PROJECTILE_PIERCING, AddValue(LevelBasedValue.perLevel(1f)))
        }

        register(LockedLoadedEnchantments.TWIRLING_CURSE, definition(
            crossbowEnchantable,
            1,
            1,
            constantCost(25),
            constantCost(50),
            8,
            EquipmentSlotGroup.MAINHAND
        )) {
            withSpecialEffect(LockedLoadedEnchantmentEffects.CROSSBOW_SPIN, LevelBasedValue.constant(20f))
        }
    }
}
package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.enchantment.Ammo
import archives.tater.omnicrossbow.enchantment.ChargedProjectileIndicator
import archives.tater.omnicrossbow.enchantment.LoadMultiple
import archives.tater.omnicrossbow.enchantment.ProjectileUncertainty
import archives.tater.omnicrossbow.util.McUnit
import archives.tater.omnicrossbow.util.validatedListCodec
import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.enchantment.ConditionalEffect
import net.minecraft.world.item.enchantment.LevelBasedValue
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets


object OmniCrossbowEnchantmentEffects {
    private inline fun <T: Any> register(path: String, init: DataComponentType.Builder<T>.() -> Unit): DataComponentType<T> = Registry.register(
        BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,
        OmniCrossbow.id(path),
        DataComponentType.builder<T>().apply(init).build()
    )

    private fun <T: Any> register(path: String, codec: Codec<T>) = register(path) {
        persistent(codec)
    }

    // First one will take priority
    @JvmField
    val LOAD_MULTIPLE = register("load_multiple", LoadMultiple.CODEC)

    @JvmField
    val PROJECTILE_FIRED_COUNT = register("projectile_fired_count", validatedListCodec(
        ConditionalEffect.codec(EnchantmentValueEffect.CODEC),
        LootContextParamSets.ENCHANTED_ENTITY
    ))

    @JvmField
    val PROJECTILE_UNCERTAINTY = register("projectile_uncertainty", validatedListCodec(
        ConditionalEffect.codec(ProjectileUncertainty.CODEC),
        LootContextParamSets.ENCHANTED_ENTITY
    ))

    @JvmField
    val PROJECTILE_VELOCITY = register("projectile_velocity", validatedListCodec(
        ConditionalEffect.codec(EnchantmentValueEffect.CODEC),
        LootContextParamSets.ENCHANTED_ENTITY
    ))

    @JvmField
    val CHARGED_PROJECTILE_INDICATOR = register("charged_projectile_indicator", ChargedProjectileIndicator.CODEC)

    @JvmField
    val CROSSBOW_COOLDOWN = register("crossbow_cooldown", validatedListCodec(
        ConditionalEffect.codec(EnchantmentValueEffect.CODEC),
        LootContextParamSets.ENCHANTED_ITEM
    ))

    @JvmField
    val AMMO = register("ammo", Ammo.CODEC.listOf())

    @JvmField
    val PROJECTILE_RICOCHET = register("projectile_ricochet", validatedListCodec(
        ConditionalEffect.codec(EnchantmentValueEffect.CODEC),
        LootContextParamSets.ENCHANTED_ITEM
    ))

    @JvmField
    val PROJECTILE_IGNORE_OWNER = register("projectile_ignore_owner", validatedListCodec(
        ConditionalEffect.codec(McUnit.CODEC),
        LootContextParamSets.ENCHANTED_ITEM
    ))

    @JvmField
    val CROSSBOW_SPIN = register("crossbow_spin", LevelBasedValue.CODEC)

    fun init() {

    }
}
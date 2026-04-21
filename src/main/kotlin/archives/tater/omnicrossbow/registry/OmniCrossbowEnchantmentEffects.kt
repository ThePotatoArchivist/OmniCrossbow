package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.util.McUnit
import archives.tater.omnicrossbow.util.validatedListCodec
import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.item.enchantment.ConditionalEffect
import net.minecraft.world.level.storage.loot.LootTable
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

    @JvmField
    val ALLOW_ANY_PROJECTILE = register("allow_any_projectile", validatedListCodec(
        ConditionalEffect.codec(McUnit.CODEC),
        LootContextParamSets.ENCHANTED_ENTITY
    ))

    @JvmField
    val DEFAULT_PROJECTILE = register("default_projectile", validatedListCodec(
        ConditionalEffect.codec(LootTable.DIRECT_CODEC),
        LootContextParamSets.ENCHANTED_ENTITY,
    ))

    fun init() {

    }
}
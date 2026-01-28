package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.context.ContextKeySet
import net.minecraft.world.item.enchantment.ConditionalEffect
import net.minecraft.world.item.enchantment.LevelBasedValue
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect
import net.minecraft.world.level.storage.loot.Validatable
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

    /**
     * @see net.minecraft.world.item.enchantment.EnchantmentEffectComponents.validatedListCodec
     */
    private fun <T : Validatable> validatedListCodec(elementCodec: Codec<T>, paramSet: ContextKeySet): Codec<MutableList<T>> =
        elementCodec.listOf().validate(Validatable.listValidatorForContext<T>(paramSet))

    // First one will take priority
    val LOAD_MULTIPLE = register("load_multiple", LevelBasedValue.CODEC.fieldOf("max_projectiles").codec())

    val PROJECTILE_FIRED_COUNT = register("projectile_fired_count", validatedListCodec(
        ConditionalEffect.codec(EnchantmentValueEffect.CODEC),
        LootContextParamSets.ENCHANTED_ENTITY
    ))

    fun init() {

    }
}
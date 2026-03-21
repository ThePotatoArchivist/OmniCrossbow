package archives.tater.lockedloaded.enchantment

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.RandomSource
import net.minecraft.world.item.enchantment.effects.AllOf
import net.minecraft.world.item.enchantment.effects.EnchantmentValueEffect

@JvmRecord
data class ProjectileUncertainty(
    val enchantmentLevel: EnchantmentValueEffect = EMPTY,
    val projectileCount: EnchantmentValueEffect = EMPTY,
) {

    fun process(enchantmentLevel: Int, projectileCount: Int, random: RandomSource, inputValue: Float) = inputValue
            .let { this.enchantmentLevel.process(enchantmentLevel, random, it) }
            .let { this.projectileCount.process(projectileCount, random, it) }

    companion object {
        val EMPTY: EnchantmentValueEffect = AllOf.valueEffects()

        val CODEC: Codec<ProjectileUncertainty> = RecordCodecBuilder.create { it.group(
            EnchantmentValueEffect.CODEC.optionalFieldOf("enchantment_level", EMPTY).forGetter(ProjectileUncertainty::enchantmentLevel),
            EnchantmentValueEffect.CODEC.optionalFieldOf("projectile_count", EMPTY).forGetter(ProjectileUncertainty::projectileCount),
        ).apply(it, ::ProjectileUncertainty) }
    }
}
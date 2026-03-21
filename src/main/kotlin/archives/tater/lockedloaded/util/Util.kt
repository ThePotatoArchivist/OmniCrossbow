@file:JvmName("OmniUtil")

package archives.tater.lockedloaded.util

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.Mth.RAD_TO_DEG
import net.minecraft.util.context.ContextKeySet
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.Validatable
import org.joml.Vector3f
import kotlin.math.atan2
import kotlin.math.sqrt
import kotlin.reflect.KProperty1

inline fun <T: Any, U: Any> getFirstEnchantmentComponent(stack: ItemStack, type: DataComponentType<T>, combine: (T, Int) -> U): U? {
    if (stack.isEmpty) return null
    for ((enchantment, level) in stack.enchantments.entrySet())
        return combine(enchantment.value().effects()[type] ?: continue, level)
    return null
}

val NON_NEGATIVE_DOUBLE: Codec<Double> = Codec.doubleRange(0.0, Double.MAX_VALUE)

val PARTICLE_OPTIONS_SHORT_CODEC: Codec<ParticleOptions> = Codec.either(
    BuiltInRegistries.PARTICLE_TYPE.byNameCodec(),
    ParticleTypes.CODEC
).comapFlatMap(
    { either -> either.map(
        { if (it is SimpleParticleType) DataResult.success(it) else DataResult.error { "$it is not a simple particle type" } },
        { DataResult.success(it) }
    ) },
    {
        if (it is SimpleParticleType) Either.left(it) else Either.right(it)
    }
)

/**
 * @see net.minecraft.world.item.enchantment.EnchantmentEffectComponents.validatedListCodec
 */
fun <T : Validatable> validatedListCodec(elementCodec: Codec<T>, paramSet: ContextKeySet): Codec<List<T>> =
    elementCodec.listOf().validate(Validatable.listValidatorForContext<T>(paramSet))

fun camelCaseToSnakeCase(str: String) = str.replace(Regex("[a-z][A-Z]"), "$1_$2").lowercase()

fun <T, V> Codec<V>.fieldOf(property: KProperty1<T, V>): RecordCodecBuilder<T, V> = fieldOf(camelCaseToSnakeCase(property.name)).forGetter(property)
fun <T, V> Codec<V>.optionalFieldOf(property: KProperty1<T, V>, default: V): RecordCodecBuilder<T, V> = optionalFieldOf(camelCaseToSnakeCase(property.name), default).forGetter(property)

fun getYRotForAngle(angle: Vector3f): Float = atan2(angle.x, angle.z) * -RAD_TO_DEG
fun getXRotForAngle(angle: Vector3f): Float = atan2(angle.y, sqrt(angle.x * angle.x + angle.z * angle.z)) * -RAD_TO_DEG
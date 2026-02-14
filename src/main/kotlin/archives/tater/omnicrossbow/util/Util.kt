@file:JvmName("OmniUtil")

package archives.tater.omnicrossbow.util

import archives.tater.omnicrossbow.network.ParticleBeamPayload
import com.llamalad7.mixinextras.sugar.ref.LocalRef
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.criterion.DataComponentMatchers
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.advancements.criterion.MinMaxBounds
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.RegistryCodecs
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.RandomSource
import net.minecraft.util.context.ContextKeySet
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.Validatable
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import io.netty.buffer.ByteBuf
import java.util.*
import java.util.function.Predicate
import kotlin.reflect.KProperty1

inline fun <T: Any, U: Any> getFirstEnchantmentComponent(stack: ItemStack, type: DataComponentType<T>, combine: (T, Int) -> U): U? {
    if (stack.isEmpty) return null
    for ((enchantment, level) in stack.enchantments.entrySet())
        return combine(enchantment.value().effects()[type] ?: continue, level)
    return null
}

val EMPTY_ITEM_PREDICATE = ItemPredicate {  }

val NON_NEGATIVE_DOUBLE: Codec<Double> = Codec.doubleRange(0.0, Double.MAX_VALUE)

val ITEM_PREDICATE_SHORT_CODEC: Codec<ItemPredicate> = Codec.either(
    ItemPredicate.CODEC,
    RegistryCodecs.homogeneousList(Registries.ITEM),
).xmap(
    { either -> either.map(
        { it },
        { ItemPredicate(Optional.of(it), MinMaxBounds.Ints.ANY, DataComponentMatchers.ANY)}
    ) },
    {
        if (it.items.isPresent && it.count.isAny && it.components.isEmpty)
            Either.right(it.items.get())
        else
            Either.left(it)
    }
)

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

val BLOCK_STATE_SHORT_CODEC: Codec<BlockState> = Codec.either(
    BuiltInRegistries.BLOCK.byNameCodec(),
    BlockState.CODEC
).xmap(
    { either -> either.map({ it.defaultBlockState() }, { it }) },
    { if (it == it.block.defaultBlockState()) Either.left(it.block) else Either.right(it) }
)

/**
 * @see net.minecraft.world.item.enchantment.EnchantmentEffectComponents.validatedListCodec
 */
fun <T : Validatable> validatedListCodec(elementCodec: Codec<T>, paramSet: ContextKeySet): Codec<List<T>> =
    elementCodec.listOf().validate(Validatable.listValidatorForContext<T>(paramSet))

fun <T: Any> Codec<Holder<T>>.valueCodec(registry: Registry<T>): Codec<T> = xmap(
    { it.value() },
    { registry.wrapAsHolder(it) }
)

inline fun <reified B: A, A> Codec<B>.narrow(crossinline error: (A) -> String): Codec<A> = flatComapMap(
    { it },
    { if (it is B) DataResult.success(it) else DataResult.error { error(it) } }
)

fun getEntitiesPierced(
    level: Level,
    start: Vec3,
    stop: Vec3,
    margin: Double,
    except: Entity?,
    predicate: Predicate<Entity>?
): List<Entity> {
    val areaBox = AABB(start, stop).inflate(margin)
    return level.getEntities(except, areaBox, { entity ->
        val box = entity.boundingBox.inflate(margin + entity.pickRadius)
        (box.contains(start) || box.clip(start, stop).isPresent) && predicate?.test(entity) != false
    })
}

fun getEntitiesPierced(level: Level, start: Vec3, stop: Vec3, margin: Double, except: Entity?): List<Entity> =
    getEntitiesPierced(level, start, stop, margin, except, null)

fun getEntitiesPierced(level: Level, start: Vec3, stop: Vec3, margin: Double, predicate: Predicate<Entity>?): List<Entity> =
    getEntitiesPierced(level, start, stop, margin, null, predicate)

fun getEntitiesPierced(level: Level, start: Vec3, stop: Vec3, margin: Double): List<Entity> =
    getEntitiesPierced(level, start, stop, margin, null, null)

fun ServerLevel.sendParticleBeam(payload: ParticleBeamPayload) {
    val packet = ClientboundCustomPayloadPacket(payload)

    for (player in players())
        sendParticles(player, false, payload.start.x, payload.start.y, payload.start.z, packet)
}

fun camelCaseToSnakeCase(str: String) = str.replace(Regex("[a-z][A-Z]"), "$1_$2").lowercase()

fun <T, V> Codec<V>.fieldOf(property: KProperty1<T, V>): RecordCodecBuilder<T, V> = fieldOf(camelCaseToSnakeCase(property.name)).forGetter(property)
fun <T, V> Codec<V>.optionalFieldOf(property: KProperty1<T, V>, default: V): RecordCodecBuilder<T, V> = optionalFieldOf(camelCaseToSnakeCase(property.name), default).forGetter(property)

fun weightedRound(value: Float, random: RandomSource) = value.toInt() + if (random.nextFloat() < value.mod(1f)) 1 else 0
fun weightedRound(value: Double, random: RandomSource) = weightedRound(value.toFloat(), random)

fun <T: Any> unverifiedUnitCodec(value: T) = object : StreamCodec<ByteBuf, T> {
    override fun decode(input: ByteBuf): T = value

    override fun encode(output: ByteBuf, value: T) {}
}

fun <T: Any> LocalRef<T?>.getOrSet(default: () -> T): T = get() ?: default().also { set(it) }

inline fun <T> MutableCollection<T>.removeFirst(predicate: (T) -> Boolean): Boolean {
    val iterator = iterator()
    for (value in iterator)
        if (predicate(value)) {
            iterator.remove()
            return true
        }
    return false
}
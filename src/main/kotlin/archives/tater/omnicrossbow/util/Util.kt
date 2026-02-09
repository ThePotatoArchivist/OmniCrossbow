@file:JvmName("OmniUtil")

package archives.tater.omnicrossbow.util

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.minecraft.advancements.criterion.DataComponentMatchers
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.advancements.criterion.MinMaxBounds
import net.minecraft.core.Holder
import net.minecraft.core.Registry
import net.minecraft.core.RegistryCodecs
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.util.context.ContextKeySet
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.loot.Validatable
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Predicate

inline fun <T: Any, U: Any> getFirstEnchantmentComponent(stack: ItemStack, type: DataComponentType<T>, combine: (T, Int) -> U): U? {
    if (stack.isEmpty) return null
    for ((enchantment, level) in stack.enchantments.entrySet())
        return combine(enchantment.value().effects()[type] ?: continue, level)
    return null
}

val EMPTY_ITEM_PREDICATE = ItemPredicate {  }

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

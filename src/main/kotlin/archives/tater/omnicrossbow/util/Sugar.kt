package archives.tater.omnicrossbow.util

import com.mojang.serialization.Codec
import net.minecraft.advancements.criterion.DataComponentMatchers
import net.minecraft.advancements.criterion.EntityPredicate
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderSet
import net.minecraft.core.TypedInstance
import net.minecraft.core.component.DataComponentHolder
import net.minecraft.core.component.DataComponentType
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.context.ContextKey
import net.minecraft.util.context.ContextKeySet
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.phys.Vec3
import java.util.*

operator fun HolderSet<Item>.contains(stack: ItemStack) = stack.`is`(this)
infix fun <T: Any> TypedInstance<T>.isOf(type: T) = `is`(type)
infix fun <T: Any> TypedInstance<T>.isIn(tag: TagKey<T>) = `is`(tag)

operator fun Vec3.plus(other: Vec3): Vec3 = add(other)
operator fun Vec3.minus(other: Vec3): Vec3 = subtract(other)
operator fun Vec3.times(scale: Double): Vec3 = scale(scale)

fun ItemPredicate(init: ItemPredicate.Builder.() -> Unit): ItemPredicate = ItemPredicate.Builder.item().apply(init).build()
fun itemPredicateBuilder(init: ItemPredicate.Builder.() -> Unit): ItemPredicate.Builder = ItemPredicate.Builder.item().apply(init)

fun ItemPredicate.Builder.withComponents(init: DataComponentMatchers.Builder.() -> Unit) {
    withComponents(DataComponentMatchers.Builder.components().apply(init).build())
}

fun DataComponentMatchers.Builder.hasAny(type: DataComponentType<*>) {
    any<DataComponentType<*>>(type)
}

fun EntityPredicate(init: EntityPredicate.Builder.() -> Unit): EntityPredicate = EntityPredicate.Builder.entity().apply(init).build()

fun LootContext(level: ServerLevel, contextKeySet: ContextKeySet, init: LootParams.Builder.() -> Unit): LootContext =
    LootParams.Builder(level)
        .apply(init)
        .create(contextKeySet)
        .let { LootContext.Builder(it) }
        .create(Optional.empty())

fun ContextKeySet(init: ContextKeySet.Builder.() -> Unit): ContextKeySet = ContextKeySet.Builder().apply(init).build()

operator fun <T: Any> LootContext.get(key: ContextKey<T>): T = getParameter(key)

typealias McUnit = net.minecraft.util.Unit

fun <T, U> ifNotNull(value: T?, transform: (T) -> U) = value?.let(transform)
infix fun <T> T?.orElse(value: () -> T) = this ?: value()

fun <T: Any> Codec<T>.singleOrList(): Codec<List<T>> = ExtraCodecs.compactListCodec(this)

operator fun DataComponentHolder.contains(type: DataComponentType<*>) = has(type)

operator fun BlockGetter.get(pos: BlockPos): BlockState = getBlockState(pos)
operator fun Level.set(pos: BlockPos, state: BlockState) = setBlockAndUpdate(pos, state)
package archives.tater.omnicrossbow.util

import net.minecraft.advancements.criterion.DataComponentMatchers
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.core.HolderSet
import net.minecraft.core.TypedInstance
import net.minecraft.core.component.DataComponentType
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.util.context.ContextKey
import net.minecraft.util.context.ContextKeySet
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.phys.Vec3
import java.util.*

operator fun HolderSet<Item>.contains(stack: ItemStack) = stack.`is`(this)
infix fun <T: Any> TypedInstance<T>.isOf(type: T) = `is`(type)
infix fun <T: Any> TypedInstance<T>.isIn(tag: TagKey<T>) = `is`(tag)

operator fun Vec3.plus(other: Vec3): Vec3 = add(other)
operator fun Vec3.times(scale: Double): Vec3 = scale(scale)

fun ItemPredicate(init: ItemPredicate.Builder.() -> Unit): ItemPredicate = ItemPredicate.Builder.item().apply(init).build()

fun ItemPredicate.Builder.withComponents(init: DataComponentMatchers.Builder.() -> Unit) {
    withComponents(DataComponentMatchers.Builder.components().apply(init).build())
}

fun DataComponentMatchers.Builder.anyOf(type: DataComponentType<*>) {
    any<DataComponentType<*>>(type)
}

fun LootContext(level: ServerLevel, contextKeySet: ContextKeySet, init: LootParams.Builder.() -> Unit): LootContext =
    LootParams.Builder(level)
        .apply(init)
        .create(contextKeySet)
        .let { LootContext.Builder(it) }
        .create(Optional.empty())

fun ContextKeySet(init: ContextKeySet.Builder.() -> Unit): ContextKeySet = ContextKeySet.Builder().apply(init).build()

operator fun <T: Any> LootContext.get(key: ContextKey<T>): T = getParameter(key)

typealias McUnit = net.minecraft.util.Unit
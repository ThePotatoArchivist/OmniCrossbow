package archives.tater.omnicrossbow.util

import net.minecraft.advancements.criterion.DataComponentMatchers
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.core.HolderSet
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

operator fun HolderSet<Item>.contains(stack: ItemStack) = stack.`is`(this)

operator fun Vec3.plus(other: Vec3): Vec3 = add(other)
operator fun Vec3.times(scale: Double): Vec3 = scale(scale)

fun ItemPredicate(init: ItemPredicate.Builder.() -> Unit): ItemPredicate = ItemPredicate.Builder.item().apply(init).build()

val EMPTY_ITEM_PREDICATE = ItemPredicate {  }

fun ItemPredicate.Builder.withComponents(init: DataComponentMatchers.Builder.() -> Unit) {
    withComponents(DataComponentMatchers.Builder.components().apply(init).build())
}
@file:JvmName("OmniUtil")

package archives.tater.omnicrossbow.util

import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import net.minecraft.advancements.criterion.DataComponentMatchers
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.advancements.criterion.MinMaxBounds
import net.minecraft.core.RegistryCodecs
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.ItemStack
import java.util.*

inline fun <T: Any, U: Any> getFirstEnchantmentComponent(stack: ItemStack, type: DataComponentType<T>, combine: (T, Int) -> U): U? {
    if (stack.isEmpty) return null
    for ((enchantment, level) in stack.enchantments.entrySet())
        return combine(enchantment.value().effects()[type] ?: continue, level)
    return null
}

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
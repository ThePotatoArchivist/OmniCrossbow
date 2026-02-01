@file:JvmName("OmniUtil")

package archives.tater.omnicrossbow.util

import net.minecraft.core.component.DataComponentType
import net.minecraft.world.item.ItemStack

inline fun <T: Any, U: Any> getFirstEnchantmentComponent(stack: ItemStack, type: DataComponentType<T>, combine: (T, Int) -> U): U? {
    if (stack.isEmpty) return null
    for ((enchantment, level) in stack.enchantments.entrySet())
        return combine(enchantment.value().effects()[type] ?: continue, level)
    return null
}

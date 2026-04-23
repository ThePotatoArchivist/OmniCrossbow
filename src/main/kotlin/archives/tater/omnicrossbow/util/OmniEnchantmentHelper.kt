package archives.tater.omnicrossbow.util

import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantmentEffects
import net.minecraft.core.component.DataComponentType
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.ConditionalEffect
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.level.storage.loot.LootContext

inline fun <T: Any, U: Any> getFirstEnchantmentComponent(stack: ItemStack, type: DataComponentType<T>, combine: (T, Int) -> U): U? {
    if (stack.isEmpty) return null
    for ((enchantment, level) in stack.enchantments.entrySet())
        return combine(enchantment.value().effects()[type] ?: continue, level)
    return null
}

inline fun <T: Any, U: Any> getFirstActiveEnchantmentComponent(stack: ItemStack, type: DataComponentType<List<ConditionalEffect<T>>>, filterData: (Int) -> LootContext, combine: (T, Int) -> U): U? {
    if (stack.isEmpty) return null
    for ((enchantment, level) in stack.enchantments.entrySet()) {
        val context = filterData(level)
        for (effect in enchantment.value().effects()[type] ?: continue)
            if (effect.matches(context))
                return combine(effect.effect, level)
    }
    return null
}

fun <T: Any> hasActiveEnchantmentComponent(stack: ItemStack, type: DataComponentType<List<ConditionalEffect<T>>>, filterData: (Int) -> LootContext) =
    getFirstActiveEnchantmentComponent(stack, type, filterData) { effect, _ -> effect } != null

fun getDefaultProjectile(heldWeapon: ItemStack, level: ServerLevel, entity: Entity): ItemStack? {
    var result: ItemStack? = null
    EnchantmentHelper.runIterationOnItem(heldWeapon) { enchantment, enchantmentLevel ->
        val filterData = Enchantment.entityContext(level, enchantmentLevel, entity, entity.position())
        Enchantment.applyEffects(
            enchantment.value().getEffects(OmniCrossbowEnchantmentEffects.DEFAULT_PROJECTILE),
            filterData
        ) { table ->
            table.getRandomItems(filterData) { result = it }
        }
    }
    return result
}
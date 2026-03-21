package archives.tater.lockedloaded.enchantment

import archives.tater.lockedloaded.registry.LockedLoadedEnchantmentEffects
import archives.tater.lockedloaded.util.getFirstEnchantmentComponent
import com.mojang.serialization.Codec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.LevelBasedValue

data class LoadMultiple(
    val maxProjectiles: LevelBasedValue
) {
    companion object {
        val CODEC: Codec<LoadMultiple> = LevelBasedValue.CODEC.fieldOf("max_projectiles").xmap(::LoadMultiple, LoadMultiple::maxProjectiles).codec()

        @JvmStatic
        fun maxProjectiles(stack: ItemStack) =
            getFirstEnchantmentComponent(stack, LockedLoadedEnchantmentEffects.LOAD_MULTIPLE) { component, level ->
                component.maxProjectiles.calculate(level)
            }?.toInt()

        @JvmStatic
        fun maxProjectilesOrDefault(stack: ItemStack) = maxProjectiles(stack) ?: 1
    }
}

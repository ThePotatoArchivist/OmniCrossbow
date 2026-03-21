package archives.tater.lockedloaded.enchantment

import archives.tater.lockedloaded.registry.LockedLoadedEnchantmentEffects
import archives.tater.lockedloaded.util.getFirstEnchantmentComponent
import com.mojang.serialization.Codec
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.LevelBasedValue

data class ChargedProjectileIndicator(
    val maxProjectiles: LevelBasedValue
) {
    companion object {
        val CODEC: Codec<ChargedProjectileIndicator> = LevelBasedValue.CODEC.fieldOf("max_projectiles").xmap(::ChargedProjectileIndicator, ChargedProjectileIndicator::maxProjectiles).codec()

        @JvmStatic
        fun maxProjectiles(stack: ItemStack) =
            getFirstEnchantmentComponent(stack, LockedLoadedEnchantmentEffects.CHARGED_PROJECTILE_INDICATOR) { component, level ->
                component.maxProjectiles.calculate(level)
            }?.toInt()

        @JvmStatic
        fun maxProjectilesOrDefault(stack: ItemStack) = maxProjectiles(stack) ?: 1
    }
}

package archives.tater.omnicrossbow.enchantment

import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantmentEffects
import archives.tater.omnicrossbow.util.contains
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderSet
import net.minecraft.core.RegistryCodecs
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.ItemEnchantments
import java.util.function.Predicate

@JvmRecord
data class Ammo(
    val items: Either<Boolean, HolderSet<Item>>,
    val held: Boolean = false
) {
    constructor(items: HolderSet<Item>, held: Boolean = false) : this(Either.right(items))

    fun matches(stack: ItemStack): Boolean = items.map({ it }, { stack in it })

    companion object {
        val CODEC: Codec<Ammo> = RecordCodecBuilder.create { it.group(
            Codec.either(Codec.BOOL, RegistryCodecs.homogeneousList(Registries.ITEM)).fieldOf("items").forGetter(Ammo::items),
            Codec.BOOL.optionalFieldOf("held", false).forGetter(Ammo::held),
        ).apply(it, ::Ammo) }

        fun anyItem(held: Boolean = true) = Ammo(Either.left(true), held)

        @JvmStatic
        fun supportedProjectiles(weapon: ItemStack, held: Boolean) = Predicate<ItemStack> { stack ->
            weapon.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY).entrySet().any { (enchantment, _) ->
                enchantment.value().getEffects(OmniCrossbowEnchantmentEffects.AMMO).any {
                    (held || !it.held) && stack != weapon && it.matches(stack)
                }
            }
        }
    }
}
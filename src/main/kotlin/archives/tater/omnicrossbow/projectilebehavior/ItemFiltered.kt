package archives.tater.omnicrossbow.projectilebehavior

import archives.tater.omnicrossbow.util.ITEM_PREDICATE_SHORT_CODEC
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.ItemStack
import kotlin.jvm.optionals.getOrNull

@JvmRecord
data class ItemFiltered<T>(
    val items: ItemPredicate,
    val value: T
) {
    companion object {
        fun <T> createCodec(valueCodec: MapCodec<T>): Codec<ItemFiltered<T>> = RecordCodecBuilder.create { it.group(
            ITEM_PREDICATE_SHORT_CODEC.fieldOf("items").forGetter(ItemFiltered<T>::items),
            valueCodec.forGetter(ItemFiltered<T>::value)
        ).apply(it, ::ItemFiltered) }

        fun <T> createCodec(valueCodec: Codec<T>): Codec<ItemFiltered<T>> = RecordCodecBuilder.create { it.group(
            ITEM_PREDICATE_SHORT_CODEC.fieldOf("items").forGetter(ItemFiltered<T>::items),
            valueCodec.fieldOf("value").forGetter(ItemFiltered<T>::value)
        ).apply(it, ::ItemFiltered) }

        fun <T: Any> getFirst(registries: HolderLookup.Provider, registry: ResourceKey<Registry<ItemFiltered<T>>>, key: ItemStack): T? = registries
            .lookupOrThrow(registry)
            .listElements()
            .map { it.value() }
            .filter { it.items.test(key) }
            .findFirst()
            .map { it.value }
            .getOrNull()
    }
}


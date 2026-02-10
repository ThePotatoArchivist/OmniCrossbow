package archives.tater.omnicrossbow.projectilebehavior

import archives.tater.omnicrossbow.util.ITEM_PREDICATE_SHORT_CODEC
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.core.HolderLookup
import net.minecraft.core.HolderSet
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.world.item.ItemStack
import java.util.Comparator.comparingInt
import java.util.stream.Stream
import kotlin.jvm.optionals.getOrNull

@JvmRecord
data class ItemFiltered<T>(
    val items: ItemPredicate,
    val value: T
) : Comparable<ItemFiltered<T>> {

    override fun compareTo(other: ItemFiltered<T>): Int =
        ITEM_PREDICATE_COMPARATOR.compare(this.items, other.items)

    companion object {
        fun <T> createCodec(valueCodec: MapCodec<T>): Codec<ItemFiltered<T>> = RecordCodecBuilder.create { it.group(
            ITEM_PREDICATE_SHORT_CODEC.fieldOf("items").forGetter(ItemFiltered<T>::items),
            valueCodec.forGetter(ItemFiltered<T>::value)
        ).apply(it, ::ItemFiltered) }

        val ITEM_PREDICATE_COMPARATOR: Comparator<ItemPredicate> =
            comparingInt<ItemPredicate> { predicate -> predicate.items.getOrNull()?.takeIf { it is HolderSet.Direct }?.size() ?: Int.MAX_VALUE }
            .thenComparingInt { if (it.items.isPresent) 0 else 1 }
            .thenComparingInt { if (it.components.exact.isEmpty) 1 else 0 }
            .then(comparingInt<ItemPredicate> { it.components.partial.size }.reversed())

        fun <T: Any> streamMatching(registries: HolderLookup.Provider, registry: ResourceKey<Registry<ItemFiltered<T>>>, key: ItemStack): Stream<T> = registries
            .lookupOrThrow(registry)
            .listElements()
            .map { it.value() }
            .filter { it.items.test(key) }
            .sorted()
            .map { it.value }

        fun <T: Any> getFirst(registries: HolderLookup.Provider, registry: ResourceKey<Registry<ItemFiltered<T>>>, key: ItemStack): T? =
            streamMatching(registries, registry, key)
            .findFirst()
            .getOrNull()
    }
}


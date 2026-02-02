package archives.tater.omnicrossbow.client.render

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.datagen.ItemTransform
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.client.renderer.block.model.ItemTransform
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.tags.TagKey
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.CropBlock
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import org.joml.Vector3f
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

object AmmoPosition : ResourceManagerReloadListener {
    private val positions = WeakHashMap<HolderLookup.Provider, Map<Item, ItemTransform>>()

    private var entries: List<Entry> = listOf()

    val PATH = OmniCrossbow.id("ammo_transforms")
    private val JSON_PATH = PATH.withSuffix(".json")

    val DEFAULT_TRANSFORM = ItemTransform(
        translation = Vector3f(-3 / 16f, 3 / 16f, 1 / 16f),
    )

    val DEFAULT_BLOCK_TRANSFORM = ItemTransform(
        rotation = Vector3f(0f, 0f, 45f),
        translation = Vector3f(-1 / 16f, 1 / 16f, 1 / 16f),
    )

    operator fun get(registries: HolderLookup.Provider, item: Item): ItemTransform = positions.getOrPut(registries) {
        val items = registries.lookupOrThrow(Registries.ITEM)
        entries.stream().flatMap { entry ->
            entry.items.stream()
                .flatMap { either -> either.map(
                    { Stream.of(it) },
                    { tag -> items.get(tag).stream()
                        .flatMap { it.stream() }
                        .map { it.value() }
                    }
                ) }
                .map { it to entry.transform }
        }.collect(Collectors.toMap(
            { it.first },
            { it.second },
            { _, second -> second }
        ))
    }[item] ?: if (item is BlockItem && item.block !is CropBlock) DEFAULT_BLOCK_TRANSFORM else DEFAULT_TRANSFORM // temporary solution

    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        positions.clear()
        entries = resourceManager.getResourceStack(JSON_PATH).flatMap { resource ->
            resource.open().use { stream ->
                JsonReader(stream.reader()).use { result ->
                    ENTRIES_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(result)).resultOrPartial {
                        OmniCrossbow.logger.info("Failed to read ammo positions: {}", it)
                    }.orElse(listOf())
                }
            }
        }
    }

    @JvmRecord
    data class Entry(val items: List<Either<Item, TagKey<Item>>>, val transform: ItemTransform) {

        constructor(transform: ItemTransform, init: ItemListBuilder.() -> Unit) :
                this(buildList { ItemListBuilder(this).init() }, transform)

        class ItemListBuilder(private val values: MutableList<Either<Item, TagKey<Item>>>) {
            fun add(item: Item) { values.add(Either.left(item)) }
            fun add(tag: TagKey<Item>) { values.add(Either.right(tag)) }

            operator fun Item.unaryPlus() { add(this) }
            operator fun TagKey<Item>.unaryPlus() { add(this) }
        }

        companion object {
            val TRANSFORM_CODEC: Codec<ItemTransform> = RecordCodecBuilder.create { it.group(
                ExtraCodecs.VECTOR3F.optionalFieldOf("rotation", Vector3f()).forGetter(ItemTransform::rotation),
                ExtraCodecs.VECTOR3F.optionalFieldOf("translation", Vector3f()).forGetter(ItemTransform::translation),
                ExtraCodecs.VECTOR3F.optionalFieldOf("scale", Vector3f(1f, 1f, 1f)).forGetter(ItemTransform::scale),
            ).apply(it, ::ItemTransform) }

            val CODEC: Codec<Entry> = RecordCodecBuilder.create { it.group(
                Codec.either(BuiltInRegistries.ITEM.byNameCodec(), TagKey.hashedCodec(Registries.ITEM)).listOf().fieldOf("items").forGetter(Entry::items),
                TRANSFORM_CODEC.fieldOf("transform").forGetter(Entry::transform)
            ).apply(it, ::Entry) }
        }
    }

    typealias Entries = List<Entry>

    val ENTRIES_CODEC: Codec<Entries> = Entry.CODEC.listOf()
}
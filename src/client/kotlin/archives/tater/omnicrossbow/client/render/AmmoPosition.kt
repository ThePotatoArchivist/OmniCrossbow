package archives.tater.omnicrossbow.client.render

import archives.tater.omnicrossbow.OmniCrossbow
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.client.renderer.block.model.ItemTransform
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.packs.resources.ResourceManager
import net.minecraft.server.packs.resources.ResourceManagerReloadListener
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.item.Item
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import org.joml.Vector3f

object AmmoPosition : ResourceManagerReloadListener {
    private var positions: Map<Item, ItemTransform> = mapOf()

    val PATH = OmniCrossbow.id("ammo_transforms")
    private val JSON_PATH = PATH.withSuffix(".json")

    val DEFAULT_TRANSFORM = ItemTransform(
        Vector3f(0f, 0f, 90f),
        Vector3f(-3 / 16f, 3 / 16f, 1 / 16f),
        Vector3f(1f, 1f, 1f)
    )

    operator fun get(item: Item) = positions[item] ?: DEFAULT_TRANSFORM

    override fun onResourceManagerReload(resourceManager: ResourceManager) {
        positions = buildMap {
            for (resource in resourceManager.getResourceStack(JSON_PATH)) {
                resource.open().use { stream ->
                    JsonReader(stream.reader()).use { result ->
                        ENTRIES_CODEC.parse(JsonOps.INSTANCE, JsonParser.parseReader(result)).ifSuccess {
                            for (entry in it)
                                for (item in entry.items)
                                    put(item, entry.transform)
                        }
                    }
                }
            }
        }
    }

    @JvmRecord
    data class Entry(val items: List<Item>, val transform: ItemTransform) {
        companion object {
            val TRANSFORM_CODEC: Codec<ItemTransform> = RecordCodecBuilder.create { it.group(
                ExtraCodecs.VECTOR3F.optionalFieldOf("rotation", Vector3f()).forGetter(ItemTransform::rotation),
                ExtraCodecs.VECTOR3F.optionalFieldOf("translation", Vector3f()).forGetter(ItemTransform::translation),
                ExtraCodecs.VECTOR3F.optionalFieldOf("scale", Vector3f(1f, 1f, 1f)).forGetter(ItemTransform::scale),
            ).apply(it, ::ItemTransform) }

            val CODEC: Codec<Entry> = RecordCodecBuilder.create { it.group(
                BuiltInRegistries.ITEM.byNameCodec().listOf().fieldOf("items").forGetter(Entry::items),
                TRANSFORM_CODEC.fieldOf("transform").forGetter(Entry::transform)
            ).apply(it, ::Entry) }
        }
    }

    typealias Entries = List<Entry>

    val ENTRIES_CODEC: Codec<Entries> = Entry.CODEC.listOf()
}
package archives.tater.omnicrossbow.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator.Pack.RegistryDependentFactory
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.client.resources.model.cuboid.ItemTransform
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.data.tags.BlockItemTagAppender
import net.minecraft.data.tags.TagAppender
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.references.BlockItemId
import net.minecraft.resources.ResourceKey
import net.minecraft.sounds.SoundEvent
import net.minecraft.tags.BlockItemTagId
import net.minecraft.tags.TagKey
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import org.joml.Vector3f

context(appender: TagAppender<T>)
operator fun <T: Any> ResourceKey<T>.unaryPlus() {
    appender.add(this)
}

context(appender: TagAppender<T>)
operator fun <T: Any> Holder.Reference<T>.unaryPlus() {
    +key()
}

context(appender: TagAppender<T>)
operator fun <T: Any> TagKey<T>.unaryPlus() {
    appender.forceAddTag(this)
}

@JvmName("unaryPlusBlock")
context(appender: TagAppender<Block>)
operator fun BlockItemTagId.unaryPlus() {
    +block
}

@JvmName("unaryPlusItem")
context(appender: TagAppender<Item>)
operator fun BlockItemTagId.unaryPlus() {
    +item
}

context(appender: BlockItemTagAppender<Item>)
operator fun BlockItemId.unaryPlus() {
    appender.add(this)
}

fun <T: Any> dynamicRegistry(name: String, registry: ResourceKey<Registry<T>>): RegistryDependentFactory<FabricDynamicRegistryProvider> = { output, registriesFuture ->
    object : FabricDynamicRegistryProvider(output, registriesFuture) {
        override fun configure(registries: HolderLookup.Provider, entries: Entries) {
            entries.addAll(registries.lookupOrThrow(registry))
        }

        override fun getName(): String = name
    }
}

fun ItemTransform(
    rotation: Vector3f = Vector3f(),
    translation: Vector3f = Vector3f(),
    scale: Vector3f = Vector3f(1f)
) = ItemTransform(rotation, translation, scale)

fun soundHolder(soundEvent: SoundEvent): Holder<SoundEvent> = BuiltInRegistries.SOUND_EVENT.wrapAsHolder(soundEvent)

operator fun <T: Any> BootstrapContext<T>.set(key: ResourceKey<T>, value: T) {
    register(key, value)
}
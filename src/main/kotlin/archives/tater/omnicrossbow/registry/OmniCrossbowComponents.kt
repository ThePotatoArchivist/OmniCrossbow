package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec

object OmniCrossbowComponents {

    private inline fun <T: Any> register(path: String, init: DataComponentType.Builder<T>.() -> Unit): DataComponentType<T> = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        OmniCrossbow.id(path),
        DataComponentType.builder<T>().apply(init).build()
    )

    private fun <T: Any> register(path: String, codec: Codec<T>, streamCodec: StreamCodec<in RegistryFriendlyByteBuf, T>, cache: Boolean = true) = register(path) {
        persistent(codec)
        networkSynchronized(streamCodec)
        if (cache) cacheEncoding()
    }



    fun init() {

    }
}
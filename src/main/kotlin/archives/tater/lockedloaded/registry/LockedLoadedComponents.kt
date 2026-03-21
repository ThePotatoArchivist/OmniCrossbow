package archives.tater.lockedloaded.registry

import archives.tater.lockedloaded.LockedLoaded
import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.world.item.component.ChargedProjectiles
import net.minecraft.world.item.component.UseEffects

object LockedLoadedComponents {

    private inline fun <T: Any> register(path: String, init: DataComponentType.Builder<T>.() -> Unit): DataComponentType<T> = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_TYPE,
        LockedLoaded.id(path),
        DataComponentType.builder<T>().apply(init).build()
    )

    private fun <T: Any> register(path: String, codec: Codec<T>, streamCodec: StreamCodec<in RegistryFriendlyByteBuf, T>, cache: Boolean = true) = register(path) {
        persistent(codec)
        networkSynchronized(streamCodec)
        if (cache) cacheEncoding()
    }

    @JvmField
    val ADDITIONAL_CHARGED_PROJECTILES = register("additional_charged_projectiles", ChargedProjectiles.CODEC, ChargedProjectiles.STREAM_CODEC)

    @JvmField
    val SPIN_CROSSBOW_USAGE = UseEffects(true, false, 1f)

    fun init() {

    }
}
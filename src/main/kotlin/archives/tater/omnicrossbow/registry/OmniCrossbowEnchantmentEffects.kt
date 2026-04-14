package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.enchantment.Ammo
import com.mojang.serialization.Codec
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponentType
import net.minecraft.core.registries.BuiltInRegistries


object OmniCrossbowEnchantmentEffects {
    private inline fun <T: Any> register(path: String, init: DataComponentType.Builder<T>.() -> Unit): DataComponentType<T> = Registry.register(
        BuiltInRegistries.ENCHANTMENT_EFFECT_COMPONENT_TYPE,
        OmniCrossbow.id(path),
        DataComponentType.builder<T>().apply(init).build()
    )

    private fun <T: Any> register(path: String, codec: Codec<T>) = register(path) {
        persistent(codec)
    }

    @JvmField
    val AMMO = register("ammo", Ammo.CODEC.listOf())

    fun init() {

    }
}
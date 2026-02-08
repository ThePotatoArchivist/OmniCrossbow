package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import archives.tater.omnicrossbow.registry.OmniCrossbowBuiltinRegistries
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries
import archives.tater.omnicrossbow.util.narrow
import archives.tater.omnicrossbow.util.valueCodec
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.resources.RegistryFileCodec

interface ProjectileAction {

    interface Inline : ProjectileAction {
        val codec: MapCodec<out Inline>
    }

    data object Default : Singleton()

    companion object {
        @JvmField
        val INLINE_CODEC: Codec<Inline> = OmniCrossbowBuiltinRegistries.PROJECTILE_ACTION_TYPE
            .byNameCodec()
            .dispatch(Inline::codec) { it }

        @JvmField
        val CODEC: Codec<ProjectileAction> = RegistryFileCodec.create(
            OmniCrossbowRegistries.PROJECTILE_ACTION,
            INLINE_CODEC.narrow { "Cannot serialize builtin action" }
        ).valueCodec(OmniCrossbowBuiltinRegistries.PROJECTILE_ACTION)
    }
}
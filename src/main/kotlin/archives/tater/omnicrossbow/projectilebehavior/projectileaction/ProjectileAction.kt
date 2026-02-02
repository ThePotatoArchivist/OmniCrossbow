package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import archives.tater.omnicrossbow.registry.OmniCrossbowBuiltinRegistries
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec

interface ProjectileAction {
    val codec: MapCodec<out ProjectileAction>

    data object Default : Singleton()

    companion object {
        val CODEC: Codec<ProjectileAction> = OmniCrossbowBuiltinRegistries.PROJECTILE_ACTION_TYPE
            .byNameCodec()
            .dispatch(ProjectileAction::codec) { it }
    }
}
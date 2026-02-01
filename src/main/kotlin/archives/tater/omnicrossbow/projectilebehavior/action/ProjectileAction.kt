package archives.tater.omnicrossbow.projectilebehavior.action

import archives.tater.omnicrossbow.registry.OmniCrossbowBuiltinRegistries
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec

sealed interface ProjectileAction {
    val codec: MapCodec<out ProjectileAction>


    data object Default : ProjectileAction {
        override val codec: MapCodec<out ProjectileAction> = MapCodec.unit(this)
    }

    companion object {
        val CODEC: Codec<ProjectileAction> = OmniCrossbowBuiltinRegistries.PROJECTILE_ACTION_TYPE
            .byNameCodec()
            .dispatch(ProjectileAction::codec) { it }
    }
}
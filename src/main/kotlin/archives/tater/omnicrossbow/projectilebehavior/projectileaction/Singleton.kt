package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import com.mojang.serialization.MapCodec

abstract class Singleton : ProjectileAction.Inline {
    override val codec: MapCodec<out Singleton> = MapCodec.unit(this)
}
package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import com.mojang.serialization.MapCodec

abstract class Singleton : ProjectileAction {
    override val codec: MapCodec<out ProjectileAction> = MapCodec.unit(this)
}
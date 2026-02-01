package archives.tater.omnicrossbow.projectilebehavior.action

import com.mojang.serialization.MapCodec

abstract class Singleton : Delegated {
    override val codec: MapCodec<out ProjectileAction> = MapCodec.unit(this)
}
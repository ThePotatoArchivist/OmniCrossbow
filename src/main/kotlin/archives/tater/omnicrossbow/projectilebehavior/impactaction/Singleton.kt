package archives.tater.omnicrossbow.projectilebehavior.impactaction

import com.mojang.serialization.MapCodec

abstract class Singleton : ImpactAction {
    override val codec: MapCodec<out ImpactAction> = MapCodec.unit(this)
}
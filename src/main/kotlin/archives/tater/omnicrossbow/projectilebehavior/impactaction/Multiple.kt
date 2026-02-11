package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.projectilebehavior.impactaction.ImpactAction.Companion.CODEC
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder

interface Multiple : ImpactAction.Inline {
    val actions: List<ImpactAction>

    companion object {
        fun <T: Multiple> createCodec(factory: (List<ImpactAction>) -> T): MapCodec<T> =
            RecordCodecBuilder.mapCodec { it.group(
                CODEC.listOf().fieldOf("actions").forGetter(Multiple::actions)
            ).apply(it, factory) }
    }
}
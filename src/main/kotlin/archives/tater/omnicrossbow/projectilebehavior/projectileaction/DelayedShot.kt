package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.valueproviders.IntProvider

@JvmRecord
data class DelayedShot(
    val delay: IntProvider,
    val action: ProjectileAction
) : ProjectileAction.Inline {

    override val codec: MapCodec<out DelayedShot> get() = CODEC

    data class Instance(
        val actions: List<DelayedShot>
    )

    companion object {
        val CODEC: MapCodec<DelayedShot> = RecordCodecBuilder.mapCodec { it.group(
            IntProvider.NON_NEGATIVE_CODEC.fieldOf("delay").forGetter(DelayedShot::delay),
            ProjectileAction.CODEC.fieldOf("action").forGetter(DelayedShot::action)
        ).apply(it, ::DelayedShot) }
    }
}
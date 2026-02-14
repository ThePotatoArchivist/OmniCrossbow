package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack

@Suppress("UnstableApiUsage")
@JvmRecord
data class DelayedShot(
    val delay: IntProvider,
    val action: ProjectileAction
) : ProjectileAction.Inline {

    override val codec: MapCodec<out DelayedShot> get() = CODEC

    data class Tracker(
        val entries: MutableList<Entry> = mutableListOf(),
        var ticksPassed: Int = 0
    ) {
        fun tick(entity: LivingEntity) {
            if (entity.level().isClientSide) return
            ticksPassed++
            val active = entries.filter { ticksPassed >= it.delay }
            entries.removeAll(active)
            for (entry in active)
                entry.action.run()
            if (entries.isEmpty())
                entity.removeAttached(OmniCrossbowAttachments.DELAYED_SHOTS)
        }

        data class Entry(
            val delay: Int,
            val action: Runnable,
            val weapon: ItemStack,
            val projectile: ItemStack,
        )
    }

    companion object {
        val CODEC: MapCodec<DelayedShot> = RecordCodecBuilder.mapCodec { it.group(
            IntProvider.NON_NEGATIVE_CODEC.fieldOf("delay").forGetter(DelayedShot::delay),
            ProjectileAction.CODEC.fieldOf("action").forGetter(DelayedShot::action)
        ).apply(it, ::DelayedShot) }.validate {
            if (it.action is DelayedShot)
                DataResult.error { "Delayed shots may not be chained" }
            else
                DataResult.success(it)
        }
    }
}
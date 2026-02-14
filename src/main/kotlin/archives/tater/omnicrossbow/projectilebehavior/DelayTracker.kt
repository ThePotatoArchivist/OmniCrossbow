package archives.tater.omnicrossbow.projectilebehavior

import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments
import archives.tater.omnicrossbow.util.removeFirst
import net.minecraft.core.component.DataComponents
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ChargedProjectiles

@Suppress("UnstableApiUsage")
class DelayTracker(
    val entries: MutableList<Entry> = mutableListOf(),
    var ticksPassed: Int = 0
) {
    fun tick(entity: LivingEntity) {
        if (entity.level().isClientSide) return

        ticksPassed++

        entries.removeIf { entry ->
            if (ticksPassed < entry.delay) return@removeIf false

            val weapon = InteractionHand.entries.firstNotNullOfOrNull { hand ->
                entity.getItemInHand(hand).takeIf { ItemStack.matches(it, entry.weapon) }
            } ?: return@removeIf true

            val projectiles = weapon[DataComponents.CHARGED_PROJECTILES]?.itemCopies()?.toMutableList() ?: return@removeIf true

            if (!projectiles.removeFirst { ItemStack.matches(it, entry.projectile) }) return@removeIf true

            entry.action.run()
            weapon[DataComponents.CHARGED_PROJECTILES] = ChargedProjectiles.ofNonEmpty(projectiles)

            true
        }

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
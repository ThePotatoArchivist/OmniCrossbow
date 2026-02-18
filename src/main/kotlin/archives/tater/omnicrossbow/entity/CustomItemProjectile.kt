package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.projectilebehavior.impactaction.ImpactAction
import archives.tater.omnicrossbow.util.contains
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult

class CustomItemProjectile(type: EntityType<out CustomItemProjectile>, level: Level) : ThrowableItemProjectile(type, level) {

    override fun getDefaultItem(): Item = Items.IRON_PICKAXE

    override fun onHit(hitResult: HitResult) {
        super.onHit(hitResult)
        val level = level() as? ServerLevel ?: return
        val originalIntangible = isItemIntangible()
        ImpactAction.streamMatching(level, item).anyMatch { 
            it.tryImpact(level, this, hitResult, item.copy()) 
        }
        if (!originalIntangible && !isItemIntangible())
            spawnAtLocation(level, item)
        discard()
    }

    private fun isItemIntangible(): Boolean = DataComponents.INTANGIBLE_PROJECTILE in item
}
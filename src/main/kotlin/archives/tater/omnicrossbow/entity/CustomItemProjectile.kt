package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.projectilebehavior.impactaction.ImpactAction
import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

class CustomItemProjectile : ThrowableItemProjectile {
    constructor(type: EntityType<out CustomItemProjectile>, level: Level) : super(type, level)

    constructor(owner: LivingEntity, level: Level, stack: ItemStack) : super(OmniCrossbowEntities.CUSTOM_ITEM_PROJECTILE, owner, level, stack)

    override fun getDefaultItem(): Item = Items.IRON_PICKAXE

    override fun onHitBlock(hitResult: BlockHitResult) {
        super.onHitBlock(hitResult)
        val level = level() as? ServerLevel ?: return
        ImpactAction.streamMatching(level, item).anyMatch { it.tryImpact(level, this, hitResult, item.copy()) }
        spawnAtLocation(level, item)
        discard()
    }

    override fun onHitEntity(hitResult: EntityHitResult) {
        super.onHitEntity(hitResult)
        val level = level() as? ServerLevel ?: return
        ImpactAction.streamMatching(level, item).anyMatch { it.tryImpact(level, this, hitResult, item.copy()) }
        spawnAtLocation(level, item)
        discard()
    }
}
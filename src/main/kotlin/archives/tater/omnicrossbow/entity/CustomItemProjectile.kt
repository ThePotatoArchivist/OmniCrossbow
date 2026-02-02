package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.projectilebehavior.impactaction.ImpactAction
import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level

class CustomItemProjectile : ThrowableItemProjectile {
    var impactAction: List<ImpactAction> = listOf()

    constructor(type: EntityType<out CustomItemProjectile>, level: Level) : super(type, level)

    constructor(owner: LivingEntity, level: Level, stack: ItemStack, impactAction: List<ImpactAction>) : super(OmniCrossbowEntities.CUSTOM_ITEM_PROJECTILE, owner, level, stack) {
        this.impactAction = impactAction
    }

    override fun getDefaultItem(): Item = Items.IRON_PICKAXE
}
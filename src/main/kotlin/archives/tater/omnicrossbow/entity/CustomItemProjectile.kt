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
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class CustomItemProjectile : ThrowableItemProjectile {
    var impactActions: ImpactAction.Series = listOf()
        private set

    constructor(type: EntityType<out CustomItemProjectile>, level: Level) : super(type, level)

    constructor(owner: LivingEntity, level: Level, stack: ItemStack, impactAction: ImpactAction.Series) : super(OmniCrossbowEntities.CUSTOM_ITEM_PROJECTILE, owner, level, stack) {
        this.impactActions = impactAction
    }

    override fun getDefaultItem(): Item = Items.IRON_PICKAXE

    override fun addAdditionalSaveData(output: ValueOutput) {
        super.addAdditionalSaveData(output)
        output.store("impact_actions", ImpactAction.SERIES_CODEC, impactActions)
    }

    override fun readAdditionalSaveData(input: ValueInput) {
        super.readAdditionalSaveData(input)
        impactActions = input.read("impact_actions", ImpactAction.SERIES_CODEC).orElse(listOf())
    }
}
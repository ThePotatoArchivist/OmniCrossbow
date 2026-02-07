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
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

class CustomItemProjectile : ThrowableItemProjectile {
    var impactBlock: ImpactAction<BlockHitResult> = ImpactAction.None
        private set
    var impactEntity: ImpactAction<EntityHitResult> = ImpactAction.None
        private set

    constructor(type: EntityType<out CustomItemProjectile>, level: Level) : super(type, level)

    constructor(owner: LivingEntity, level: Level, stack: ItemStack, impactBlock: ImpactAction<BlockHitResult>, impactEntity: ImpactAction<EntityHitResult>) : super(OmniCrossbowEntities.CUSTOM_ITEM_PROJECTILE, owner, level, stack) {
        this.impactBlock = impactBlock
        this.impactEntity = impactEntity
    }

    override fun getDefaultItem(): Item = Items.IRON_PICKAXE

    override fun onHitBlock(hitResult: BlockHitResult) {
        super.onHitBlock(hitResult)
        val level = level() as? ServerLevel ?: return
        impactBlock.tryImpact(level, this, hitResult)
        spawnAtLocation(level, item)
        discard()
    }

    override fun onHitEntity(hitResult: EntityHitResult) {
        super.onHitEntity(hitResult)
        val level = level() as? ServerLevel ?: return
        impactEntity.tryImpact(level, this, hitResult)
        spawnAtLocation(level, item)
        discard()
    }

    override fun addAdditionalSaveData(output: ValueOutput) {
        super.addAdditionalSaveData(output)
        output.store("impact_block", ImpactAction.BLOCK_CODEC, impactBlock)
        output.store("impact_entity", ImpactAction.ENTITY_CODEC, impactEntity)
    }

    override fun readAdditionalSaveData(input: ValueInput) {
        super.readAdditionalSaveData(input)
        impactBlock = input.read("impact_block", ImpactAction.BLOCK_CODEC).orElse(ImpactAction.None)
        impactEntity = input.read("impact_block", ImpactAction.ENTITY_CODEC).orElse(ImpactAction.None)
    }
}
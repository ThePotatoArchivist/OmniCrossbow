package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.projectilebehavior.ImpactAction
import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import archives.tater.omnicrossbow.util.LootContext
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

class CustomItemProjectile : ThrowableItemProjectile {
    var impactBlock: ImpactAction.Series<BlockHitResult> = listOf()
        private set
    var impactEntity: ImpactAction.Series<EntityHitResult> = listOf()
        private set

    constructor(type: EntityType<out CustomItemProjectile>, level: Level) : super(type, level)

    constructor(owner: LivingEntity, level: Level, stack: ItemStack, impactBlock: ImpactAction.Series<BlockHitResult>, impactEntity: ImpactAction.Series<EntityHitResult>) : super(OmniCrossbowEntities.CUSTOM_ITEM_PROJECTILE, owner, level, stack) {
        this.impactBlock = impactBlock
        this.impactEntity = impactEntity
    }

    override fun getDefaultItem(): Item = Items.IRON_PICKAXE

    override fun onHitBlock(hitResult: BlockHitResult) {
        super.onHitBlock(hitResult)
        val level = level() as? ServerLevel ?: return
        val context = LootContext(level, ImpactAction.BLOCK_CONTEXT) {
            withParameter(LootContextParams.BLOCK_STATE, level.getBlockState(hitResult.blockPos))
            withParameter(LootContextParams.ORIGIN, hitResult.location)
            withOptionalParameter(LootContextParams.ATTACKING_ENTITY, owner?.getEntity(level, Entity::class.java))
            withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, this@CustomItemProjectile)
            withParameter(LootContextParams.TOOL, item)
        }
        for (effect in impactBlock)
            if (effect.matches(context))
                effect.effect.tryImpact(level, this, hitResult)
        spawnAtLocation(level, item)
        discard()
    }

    override fun onHitEntity(hitResult: EntityHitResult) {
        super.onHitEntity(hitResult)
        val level = level() as? ServerLevel ?: return
        val context = LootContext(level, ImpactAction.ENTITY_CONTEXT) {
            withParameter(LootContextParams.TARGET_ENTITY, hitResult.entity)
            withParameter(LootContextParams.ORIGIN, hitResult.location)
            withOptionalParameter(LootContextParams.ATTACKING_ENTITY, owner?.getEntity(level, Entity::class.java))
            withParameter(LootContextParams.DIRECT_ATTACKING_ENTITY, this@CustomItemProjectile)
            withParameter(LootContextParams.TOOL, item)
        }
        for (effect in impactEntity)
            if (effect.matches(context))
                effect.effect.tryImpact(level, this, hitResult)
        spawnAtLocation(level, item)
        discard()
    }

    override fun addAdditionalSaveData(output: ValueOutput) {
        super.addAdditionalSaveData(output)
        output.store("impact_block", ImpactAction.BLOCK_SERIES_CODEC, impactBlock)
        output.store("impact_entity", ImpactAction.ENTITY_SERIES_CODEC, impactEntity)
    }

    override fun readAdditionalSaveData(input: ValueInput) {
        super.readAdditionalSaveData(input)
        impactBlock = input.read("impact_block", ImpactAction.BLOCK_SERIES_CODEC).orElse(listOf())
        impactEntity = input.read("impact_block", ImpactAction.ENTITY_SERIES_CODEC).orElse(listOf())
    }
}
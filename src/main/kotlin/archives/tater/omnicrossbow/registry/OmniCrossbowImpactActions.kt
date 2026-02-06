package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.mixin.behavior.MobInvoker
import archives.tater.omnicrossbow.projectilebehavior.impactaction.ImpactAction
import archives.tater.omnicrossbow.util.isIn
import net.minecraft.core.Registry
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.ItemStackTemplate
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

object OmniCrossbowImpactActions {

    private fun registerBlock(path: String, action: ImpactAction<BlockHitResult>): ImpactAction<BlockHitResult> =
        Registry.register(OmniCrossbowBuiltinRegistries.BLOCK_IMPACT_ACTION, OmniCrossbow.id(path), action)

    private fun registerEntity(path: String, action: ImpactAction<EntityHitResult>): ImpactAction<EntityHitResult> =
        Registry.register(OmniCrossbowBuiltinRegistries.ENTITY_IMPACT_ACTION, OmniCrossbow.id(path), action)

    private fun registerBoth(path: String, action: ImpactAction<HitResult>) = action.also {
        registerBlock(path, it)
        registerEntity(path, it)
    }

    val BREAK_BLOCK = registerBlock("break_block") { level, projectile, hit ->
        level.destroyBlock(hit.blockPos, true, projectile.owner)
        true
    }

    val CONSUME_ITEM = registerEntity("consume_item") { level, projectile, hit ->
        val entity = hit.entity as? LivingEntity ?: return@registerEntity false
        val stack = projectile.item.finishUsingItem(level, entity)
        if (entity is Player) entity.handleExtraItemsCreatedOnUse(stack)
        else entity.spawnAtLocation(level, stack)
        true
    }

    val EQUIP = registerEntity("equip") { level, projectile, hit ->
        // todo data driven
        val entity = hit.entity as? LivingEntity ?: return@registerEntity false
        if (!entity.canPickUpLoot() && !(entity isIn OmniCrossbowTags.CAN_ALWAYS_EQUIP)) return@registerEntity false
        val stack = projectile.item
        val slot = entity.getEquipmentSlotForItem(stack)
        if (slot == EquipmentSlot.MAINHAND || !entity.isEquippableInSlot(stack, slot)) return@registerEntity false
        val oldStack = entity.getItemBySlot(slot)
        entity.spawnAtLocation(level, oldStack)
        when (entity) {
            is MobInvoker -> entity.invokeSetItemSlotAndDropWhenKilled(slot, stack)
            else -> entity.setItemSlot(slot, stack)
        }
        projectile.item = ItemStack.EMPTY
        true
    }

    val SMALL_EXPLOSION = registerBoth("small_explosion") { level, projectile, hit ->
        level.explode(projectile, hit.location.x, hit.location.y, hit.location.z, 1f, true, Level.ExplosionInteraction.TNT)
        true
    }

    val ITEM_PARTICLE = registerBoth("item_particle") { level, projectile, hit ->
        level.sendParticles(ItemParticleOption(ParticleTypes.ITEM, ItemStackTemplate.fromNonEmptyStack(projectile.item)), hit.location.x, hit.location.y, hit.location.z, 8, 0.0, 0.0, 0.0, 0.1)
        true
    }

    val SHRINK = registerBoth("shrink") { _, projectile, _ ->
        projectile.item.shrink(1)
        true
    }

    fun init() {
        registerBoth("none", ImpactAction.None)
    }
}
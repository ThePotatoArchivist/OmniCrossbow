package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.mixin.behavior.MobInvoker
import archives.tater.omnicrossbow.projectilebehavior.impactaction.*
import archives.tater.omnicrossbow.util.isIn
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

object OmniCrossbowImpactActions {

    private fun registerBlock(path: String, codec: MapCodec<out ImpactAction.Inline<BlockHitResult>>) {
        Registry.register(OmniCrossbowBuiltinRegistries.BLOCK_IMPACT_ACTION_TYPE, OmniCrossbow.id(path), codec)
    }

    private fun registerBlock(path: String, action: ImpactAction<BlockHitResult>): ImpactAction<BlockHitResult> =
        Registry.register(OmniCrossbowBuiltinRegistries.BLOCK_IMPACT_ACTION, OmniCrossbow.id(path), action)

    private fun registerEntity(path: String, codec: MapCodec<out ImpactAction.Inline<EntityHitResult>>) {
        Registry.register(OmniCrossbowBuiltinRegistries.ENTITY_IMPACT_ACTION_TYPE, OmniCrossbow.id(path), codec)
    }

    private fun registerEntity(path: String, action: ImpactAction<EntityHitResult>): ImpactAction<EntityHitResult> =
        Registry.register(OmniCrossbowBuiltinRegistries.ENTITY_IMPACT_ACTION, OmniCrossbow.id(path), action)

    private fun registerBoth(path: String, codec: MapCodec<out ImpactAction.Inline<HitResult>>) {
        registerBlock(path, codec)
        registerEntity(path, codec)
    }

    private fun registerBoth(path: String, action: ImpactAction<HitResult>) = action.also {
        registerBlock(path, it)
        registerEntity(path, it)
    }

    private fun registerComposite(path: String, type: CompositeType<*, *>) {
        registerBlock(path, type.blockInstanceCodec)
        registerEntity(path, type.entityInstanceCodec)
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

    val SHRINK = registerBoth("shrink") { _, projectile, _ ->
        projectile.item.shrink(1)
        true
    }

    fun init() {
        registerBoth("none", ImpactAction.None)
        registerBoth("item_particle", ItemParticle.CODEC)
        registerBoth("explode", Explode.CODEC)
        registerComposite("all_of", AllOf)
        registerComposite("any_of", AnyOf)
        registerComposite("conditional", Conditional)
        registerBlock("loot_condition", LootCondition.BLOCK_CODEC)
        registerEntity("loot_condition", LootCondition.ENTITY_CODEC)
    }
}
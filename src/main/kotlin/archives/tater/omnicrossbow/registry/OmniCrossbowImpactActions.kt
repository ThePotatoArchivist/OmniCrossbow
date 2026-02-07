package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.entity.createFakePlayer
import archives.tater.omnicrossbow.mixin.behavior.MobInvoker
import archives.tater.omnicrossbow.projectilebehavior.impactaction.*
import net.fabricmc.fabric.api.event.player.AttackBlockCallback
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult


object OmniCrossbowImpactActions {

    private fun register(path: String, codec: MapCodec<out ImpactAction.Inline>) {
        Registry.register(OmniCrossbowBuiltinRegistries.IMPACT_ACTION_TYPE, OmniCrossbow.id(path), codec)
    }

    private fun register(path: String, action: ImpactAction): ImpactAction =
        Registry.register(OmniCrossbowBuiltinRegistries.IMPACT_ACTION, OmniCrossbow.id(path), action)

    private inline fun registerBlock(path: String, crossinline action: (level: ServerLevel, projectile: CustomItemProjectile, hit: BlockHitResult, originalItem: ItemStack) -> Boolean): ImpactAction =
        register(path) { level, projectile, hit, originalItem ->
            hit is BlockHitResult && action(level, projectile, hit, originalItem)
        }

    private inline fun registerEntity(path: String, crossinline action: (level: ServerLevel, projectile: CustomItemProjectile, hit: EntityHitResult, originalItem: ItemStack) -> Boolean): ImpactAction =
        register(path) { level, projectile, hit, originalItem ->
            hit is EntityHitResult && action(level, projectile, hit, originalItem)
        }

    val BREAK_BLOCK = registerBlock("break_block") { level, projectile, hit, _ ->
        val state = level.getBlockState(hit.blockPos)
        val blockEntity = if (state.hasBlockEntity()) level.getBlockEntity(hit.blockPos) else null
        Block.dropResources(state, level, hit.blockPos, blockEntity, projectile.owner, projectile.item)
        level.destroyBlock(hit.blockPos, false, projectile.owner)
        true
    }

    val CONSUME_ITEM = registerEntity("consume_item") { level, projectile, hit, _ ->
        val entity = hit.entity as? LivingEntity ?: return@registerEntity false
        val stack = projectile.item.finishUsingItem(level, entity)
        if (entity is Player) entity.handleExtraItemsCreatedOnUse(stack)
        else entity.spawnAtLocation(level, stack)
        true
    }

    val EQUIP = registerEntity("equip") { level, projectile, hit, _ ->
        val entity = hit.entity as? LivingEntity ?: return@registerEntity false
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

    val SHRINK = register("shrink") { _, projectile, _, _ ->
        projectile.item.shrink(1)
        true
    }

    val DURABILITY_DAMAGE = register("durability_damage") { level, projectile, hit, _ ->
        val stack = projectile.item
        if (!stack.isDamageableItem) return@register false
        stack.hurtAndBreak(1, level, null) {
            level.sendParticles(ItemParticleOption(ParticleTypes.ITEM, it), hit.location.x, hit.location.y, hit.location.z, 8, 0.0, 0.0, 0.0, 0.1)
        }
        true
    }

    val USE_ITEM = register("use_item") { level, projectile, hit, _ ->
        val result = when (hit) {
            is BlockHitResult -> {
                val player = createFakePlayer(level, projectile)
                level.getBlockState(hit.blockPos).useItemOn(projectile.item, level, player, InteractionHand.MAIN_HAND, hit)
                    .takeUnless { !it.consumesAction() }
                    ?: projectile.item.useOn(UseOnContext(player, InteractionHand.MAIN_HAND, hit))
            }
            is EntityHitResult -> {
                hit.entity.interact(createFakePlayer(level, projectile), InteractionHand.MAIN_HAND, hit.location)
            }
            else -> return@register false
        }
        (result as? InteractionResult.Success)?.heldItemTransformedTo()?.let {
            projectile.item = it
        }
        result.consumesAction()
    }

    fun init() {
        register("none", ImpactAction.None)
        register("item_particle", ItemParticle.CODEC)
        register("explode", Explode.CODEC)
        register("all_of", AllOf.CODEC)
        register("any_of", AnyOf.CODEC)
        register("conditional", Conditional.CODEC)
        register("loot_condition", LootCondition.CODEC)
        AttackBlockCallback.EVENT
    }
}
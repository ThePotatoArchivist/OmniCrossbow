package archives.tater.omnicrossbow.projectilebehavior.action

import archives.tater.omnicrossbow.mixin.behavior.BoatItemAccessor
import archives.tater.omnicrossbow.mixin.behavior.FallingBlockEntityAccessor
import archives.tater.omnicrossbow.mixin.behavior.MinecartItemAccessor
import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.entity.vehicle.boat.AbstractBoat
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3

interface SpawnEntity<T: Entity> : Delegated {

    fun getType(projectile: ItemStack): EntityType<out T>?

    fun T.process(
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack
    ) {}

    override fun shoot(
        pos: Vec3,
        velocity: Vec3,
        level: ServerLevel,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack
    ) {
        getType(projectile)?.create(
            level,
            EntityType.createDefaultStackConfig(level, projectile, shooter),
            BlockPos.containing(pos),
            EntitySpawnReason.SPAWN_ITEM_USE,
            false,
            false
        )?.apply {
            setPos(pos)
            deltaMovement = velocity
            process(shooter, weapon, projectile)
        }?.let { level.addFreshEntity(it) }
    }

    @JvmRecord
    data class Direct(val type: EntityType<*>) : SpawnEntity<Entity> {

        override fun getType(projectile: ItemStack): EntityType<*> = type

        override val codec: MapCodec<out ProjectileAction> get() = CODEC

        companion object {
            val CODEC: MapCodec<Direct> = EntityType.CODEC.fieldOf("entity_type")
                .xmap(::Direct, Direct::type)
        }
    }

    @JvmRecord
    data class FallingBlock(val state: BlockState) : SpawnEntity<FallingBlockEntity> {
        override fun getType(projectile: ItemStack): EntityType<out FallingBlockEntity> = EntityType.FALLING_BLOCK

        override fun FallingBlockEntity.process(shooter: LivingEntity, weapon: ItemStack, projectile: ItemStack) {
            this as FallingBlockEntityAccessor
            blockState = ((projectile.item as? BlockItem)?.block ?: Blocks.SAND).defaultBlockState()
            blocksBuilding = true
            xo = position().x
            yo = position().y
            zo = position().z
            startPos = blockPosition()
        }

        override val codec: MapCodec<out ProjectileAction> get() = CODEC

        companion object {
            val CODEC: MapCodec<FallingBlock> = BlockState.CODEC.fieldOf("state").xmap(::FallingBlock, FallingBlock::state)
        }
    }

    data object Boat : Singleton(), SpawnEntity<AbstractBoat> {
        override fun getType(projectile: ItemStack): EntityType<out AbstractBoat>? =
            (projectile.item as? BoatItemAccessor)?.entityType
    }

    data object Minecart : Singleton(), SpawnEntity<AbstractMinecart> {
        override fun getType(projectile: ItemStack): EntityType<out AbstractMinecart>? =
            (projectile.item as? MinecartItemAccessor)?.type
    }

    data object FromEgg : Singleton(), SpawnEntity<Entity> {
        override fun getType(projectile: ItemStack): EntityType<*>? = SpawnEggItem.getType(projectile)
    }
}
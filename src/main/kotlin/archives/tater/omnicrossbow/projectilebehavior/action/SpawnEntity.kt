package archives.tater.omnicrossbow.projectilebehavior.action

import archives.tater.omnicrossbow.mixin.behavior.*
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.*
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.entity.vehicle.boat.AbstractBoat
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import java.util.*

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
    data class FallingBlock(
        val state: BlockState,
        val hurtsEntities: Optional<HurtsEntities> = Optional.empty(),
    ) : SpawnEntity<FallingBlockEntity> {

        constructor(state: BlockState, damagePerDistance: Float, damageMax: Int)
            : this(state, Optional.of(HurtsEntities(damagePerDistance, damageMax)))

        override fun getType(projectile: ItemStack): EntityType<out FallingBlockEntity> = EntityType.FALLING_BLOCK

        override fun FallingBlockEntity.process(shooter: LivingEntity, weapon: ItemStack, projectile: ItemStack) {
            this as FallingBlockEntityAccessor
            blockState = state
            blocksBuilding = true
            xo = position().x
            yo = position().y
            zo = position().z
            startPos = blockPosition()
            if (projectile.has(DataComponents.INTANGIBLE_PROJECTILE))
                disableDrop()
            (state.block as? FallingBlockInvoker)?.invokeFalling(this)
            hurtsEntities.ifPresent {
                setHurtsEntities(it.damagePerDistance, it.damageMax)
            }
        }

        override val codec: MapCodec<out ProjectileAction> get() = CODEC

        @JvmRecord
        data class HurtsEntities(val damagePerDistance: Float, val damageMax: Int) {
            companion object {
                val CODEC: Codec<HurtsEntities> = RecordCodecBuilder.create { it.group(
                    Codec.floatRange(0f, Float.MAX_VALUE).fieldOf("damage_per_distance").forGetter(HurtsEntities::damagePerDistance),
                    Codec.intRange(0, Int.MAX_VALUE).fieldOf("damage_max").forGetter(HurtsEntities::damageMax),
                ).apply(it, ::HurtsEntities) }
            }
        }

        companion object {
            val CODEC: MapCodec<FallingBlock> = RecordCodecBuilder.mapCodec { it.group(
                BlockState.CODEC.fieldOf("state").forGetter(FallingBlock::state),
                HurtsEntities.CODEC.optionalFieldOf("hurts_entities").forGetter(FallingBlock::hurtsEntities)
            ).apply(it, ::FallingBlock) }
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

    data object FromBucket : Singleton(), SpawnEntity<Mob> {
        override fun getType(projectile: ItemStack): EntityType<out Mob>? =
            (projectile.item as? MobBucketItemAccessor)?.type
    }

    data object Item : Singleton(), SpawnEntity<ItemEntity> {
        override fun getType(projectile: ItemStack): EntityType<out ItemEntity> = EntityType.ITEM

        override fun ItemEntity.process(shooter: LivingEntity, weapon: ItemStack, projectile: ItemStack) {
            this as ItemEntityAccessor
            item = projectile.copy()
            if (projectile.has(DataComponents.INTANGIBLE_PROJECTILE)) {
                setNeverPickUp()
                age = 6000 - 10 * 20
            } else
                setDefaultPickUpDelay()
        }
    }
}
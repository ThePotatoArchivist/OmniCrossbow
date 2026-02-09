package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import archives.tater.omnicrossbow.mixin.behavior.FallingBlockEntityAccessor
import archives.tater.omnicrossbow.mixin.behavior.FallingBlockInvoker
import archives.tater.omnicrossbow.mixin.behavior.ItemEntityAccessor
import archives.tater.omnicrossbow.util.contains
import com.mojang.logging.LogUtils
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ProblemReporter
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.EntityBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.TagValueInput
import net.minecraft.world.level.storage.TagValueOutput
import net.minecraft.world.phys.Vec3
import java.util.*

fun interface SpawnEntity<T: Entity> : Delegated {

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
            EntitySpawnReason.TRIGGERED,
            false,
            false
        )?.apply {
            snapTo(pos, velocity.rotation().y, 90f)
            deltaMovement = velocity
            process(shooter, weapon, projectile)
        }?.let { level.addFreshEntity(it) }
    }

    @JvmRecord
    data class Direct(val entityType: EntityType<*>) : SpawnEntity<Entity>, ProjectileAction.Inline {

        override fun getType(projectile: ItemStack): EntityType<*> = entityType

        override val codec: MapCodec<out Direct> get() = CODEC

        companion object {
            val CODEC: MapCodec<Direct> = EntityType.CODEC.fieldOf("entity")
                .xmap(::Direct, Direct::entityType)
        }
    }

    @JvmRecord
    data class FallingBlock(
        val state: BlockState,
        val hurtsEntities: Optional<HurtsEntities> = Optional.empty(),
    ) : SpawnEntity<FallingBlockEntity>, ProjectileAction.Inline {

        constructor(state: BlockState, damagePerDistance: Float, damageMax: Int)
            : this(state, Optional.of(HurtsEntities(damagePerDistance, damageMax)))

        override fun getType(projectile: ItemStack): EntityType<out FallingBlockEntity> = EntityType.FALLING_BLOCK

        override fun FallingBlockEntity.process(shooter: LivingEntity, weapon: ItemStack, projectile: ItemStack) {
            this as FallingBlockEntityAccessor
            blockState = state
            if (state.hasBlockEntity()) (state.block as? EntityBlock)?.newBlockEntity(BlockPos.ZERO, state)?.let { blockEntity ->
                blockData = ProblemReporter.ScopedCollector(LogUtils.getLogger()).use { problemReporter ->
                    blockEntity.loadCustomOnly(TagValueInput.create(problemReporter, shooter.registryAccess(), projectile[DataComponents.BLOCK_ENTITY_DATA]?.copyTagWithoutId() ?: CompoundTag()))
                    blockEntity.applyComponentsFromItemStack(projectile)
                    TagValueOutput.createWithContext(problemReporter, shooter.registryAccess())
                        .also { blockEntity.saveWithId(it) }
                        .buildResult()
                }
            }
            blocksBuilding = true
            xo = position().x
            yo = position().y
            zo = position().z
            startPos = blockPosition()
            if (DataComponents.INTANGIBLE_PROJECTILE in projectile)
                disableDrop()
            (state.block as? FallingBlockInvoker)?.invokeFalling(this)
            hurtsEntities.ifPresent {
                setHurtsEntities(it.damagePerDistance, it.damageMax)
            }
        }

        override val codec: MapCodec<out FallingBlock> get() = CODEC

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

    data object Item : Singleton(), SpawnEntity<ItemEntity> {
        override fun getType(projectile: ItemStack): EntityType<out ItemEntity> = EntityType.ITEM

        override fun ItemEntity.process(shooter: LivingEntity, weapon: ItemStack, projectile: ItemStack) {
            this as ItemEntityAccessor
            item = projectile.copy()
            if (DataComponents.INTANGIBLE_PROJECTILE in projectile) {
                setNeverPickUp()
                age = 6000 - 10 * 20
            } else
                setDefaultPickUpDelay()
        }
    }
}
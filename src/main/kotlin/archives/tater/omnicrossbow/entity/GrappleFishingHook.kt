package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.network.addMovementClient
import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments
import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import archives.tater.omnicrossbow.registry.OmniCrossbowEntityDataSerializers
import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import archives.tater.omnicrossbow.util.*
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.component.DataComponents
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionHand
import net.minecraft.world.entity.*
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.ChargedProjectiles
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.SupportType
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.Vec3
import java.util.*

@Suppress("UnstableApiUsage")
class GrappleFishingHook(type: EntityType<out Projectile>, level: Level) : Projectile(type, level) {

    private var hookedEntityRef by HOOKED_ENTITY
    private var _hookedEntity by ::hookedEntityRef
    var hookedEntity
        get() = _hookedEntity
        set(value) {
            _hookedEntity?.getAttachedOrCreate(OmniCrossbowAttachments.CONNECTED_GRAPPLE_HOOKS)?.remove(this)
            _hookedEntity = value
            value?.getAttachedOrCreate(OmniCrossbowAttachments.CONNECTED_GRAPPLE_HOOKS)?.add(this)
        }
    var hookedBlockPos: BlockPos? = null
    var hookedBlockFace by HOOKED_BLOCK_FACE
        private set
    var pullingOwner by PULLING_OWNER
        private set
    var projectileItem: ItemStack = ItemStack.EMPTY
        private set

    val isPulling get() = hookedEntity != null || hookedBlockFace != null

    constructor(level: Level, owner: LivingEntity, projectile: ItemStack) : this(OmniCrossbowEntities.GRAPPLE_FISHING_HOOK, level) {
        setOwner(owner)
        setPos(owner.x, owner.eyeY - 0.1, owner.z)
        projectileItem = projectile
    }

    override fun defineSynchedData(entityData: SynchedEntityData.Builder) {
        entityData.define(HOOKED_ENTITY, Optional.empty())
        entityData.define(HOOKED_BLOCK_FACE, Optional.empty())
        entityData.define(PULLING_OWNER, false)
    }

    override fun shouldRender(camX: Double, camY: Double, camZ: Double): Boolean = true

    override fun setOwner(owner: EntityReference<Entity>?) {
        getOwner()?.getAttachedOrCreate(OmniCrossbowAttachments.CONNECTED_GRAPPLE_HOOKS)?.remove(this)
        super.setOwner(owner)
        getOwner()?.getAttachedOrCreate(OmniCrossbowAttachments.CONNECTED_GRAPPLE_HOOKS)?.add(this)
    }

    private fun cleanupRefs() {
        _hookedEntity?.getAttachedOrCreate(OmniCrossbowAttachments.CONNECTED_GRAPPLE_HOOKS)?.remove(this)
        getOwner()?.getAttachedOrCreate(OmniCrossbowAttachments.CONNECTED_GRAPPLE_HOOKS)?.remove(this)
    }

    override fun onRemoval(reason: RemovalReason) {
        cleanupRefs()
    }

    override fun onClientRemoval() {
        cleanupRefs()
    }

    override fun tick() {
        val owner = getOwner() as? LivingEntity ?: run {
            discard()
            return
        }
        if (!level().isClientSide && (
            owner.level() != this.level()
            || distanceToSqr(owner) > MAX_DISTANCE * MAX_DISTANCE
            || InteractionHand.entries.none { hand ->
                owner.getItemInHand(hand)[DataComponents.CHARGED_PROJECTILES]?.itemCopies()?.any { ItemStack.matches(it, projectileItem) } == true
            })) {
            discard()
            return
        }

        super.tick()

        val hookedEntity = hookedEntity
        val hookedBlockFace = hookedBlockFace

        when {
            hookedEntity != null -> {
                if (!hookedEntity.canInteractWithLevel() || hookedEntity.level() != this.level()) {
                    discard()
                    return
                }

                setPos(hookedEntity.x, hookedEntity.getY(0.8), hookedEntity.z)

                if (!level().isClientSide)
                    pullingOwner = getWeightScore(hookedEntity) > getWeightScore(owner)

                if (pullingOwner)
                    pullOrDisconnect(owner, hookedEntity.eyePosition)
                else {
                    if (hookedEntity is LivingEntity && hookedEntity !is Player)
                        hookedEntity[OmniCrossbowAttachments.GRAPPLE_NO_HIT_COOLDOWN] = NO_HIT_TICKS
                    pullOrDisconnect(hookedEntity, owner.eyePosition)
                }
            }
            hookedBlockFace != null -> {
                val hookedBlockPos = hookedBlockPos
                if (!level().isClientSide && (hookedBlockPos == null || !level()[hookedBlockPos].isFaceSturdy(level(), hookedBlockPos, hookedBlockFace, SupportType.CENTER))) {
                    discard()
                    return
                }

                pullOrDisconnect(owner, position())
            }
            else -> {
                val hit = ProjectileUtil.getHitResultOnMoveVector(this, ::canHitEntity)
                hitTargetOrDeflectSelf(hit)
                updateRotation()

                val movement = deltaMovement
                setPos(position() + movement)
                return
            }
        }
    }

    fun pullOrDisconnect(entity: Entity, target: Vec3) {
        val offset = target - if (hookedBlockFace == Direction.UP) entity.position() else entity.eyePosition

        if (!level().isClientSide && offset.lengthSqr() < MIN_DISTANCE * MIN_DISTANCE) {
            if (hookedBlockFace?.axis == Direction.Axis.X || hookedBlockFace?.axis == Direction.Axis.Z) {
                entity.addMovementClient(Vec3(0.0, DISCONNECT_BOOST, 0.0), true)
            } else if (hookedEntity != null) {
                entity.deltaMovement -= offset.normalize() * (entity.deltaMovement * offset.normalize())
                entity.needsSync
            }
            disconnect()
            return
        }

        pullTowards(entity, offset)
    }

    fun disconnect() {
        val owner = getOwner() as? LivingEntity
        if (owner != null)
            for (hand in InteractionHand.entries) {
                val stack = owner.getItemInHand(hand)
                val items = stack[DataComponents.CHARGED_PROJECTILES]?.itemCopies()?.toMutableList() ?: continue
                if (items.removeFirst { ItemStack.matches(it, projectileItem) }) {
                    stack[DataComponents.CHARGED_PROJECTILES] = ChargedProjectiles.ofNonEmpty(items)
                    owner.giveOrDrop(projectileItem)
                    break
                }
            }

        discard()
    }

    fun pullTowards(entity: Entity, offset: Vec3) {
        val direction = offset.normalize()
        val movement = entity.deltaMovement
        val directionVelocity = movement * direction
        if (directionVelocity < MAX_PULL_SPEED)
            entity.deltaMovement += direction * (PULL_FACTOR * (MAX_PULL_SPEED - directionVelocity))
    }

    override fun onHitBlock(hitResult: BlockHitResult) {
        super.onHitBlock(hitResult)
        if (level().isClientSide) return
        if (!level()[hitResult.blockPos].isFaceSturdy(level(), hitResult.blockPos, hitResult.direction, SupportType.CENTER)) {
            discard()
            return
        }
        setHooked(pos = hitResult.blockPos, face = hitResult.direction)
        setPos(hitResult.blockPos.center.relative(hitResult.direction, 0.5))
        deltaMovement = Vec3.ZERO
        level().playSound(null, getOwner() ?: this, SoundEvents.FISHING_BOBBER_RETRIEVE, soundSource, 1f, 1f)
    }

    override fun onHitEntity(hitResult: EntityHitResult) {
        super.onHitEntity(hitResult)
        if (level().isClientSide) return
        setHooked(entity = hitResult.entity)
        deltaMovement = Vec3.ZERO
        level().playSound(null, getOwner() ?: this, SoundEvents.FISHING_BOBBER_RETRIEVE, soundSource, 1f, 1f)
    }

    private fun setHooked(pos: BlockPos? = null, face: Direction? = null, entity: Entity? = null) {
        hookedBlockPos = pos
        hookedBlockFace = face
        hookedEntity = entity
    }

    companion object {
        val HOOKED_ENTITY = defineSynchedEntityData<GrappleFishingHook, _>(OmniCrossbowEntityDataSerializers.OPTIONAL_ENTITY_REFERENCE)
        val HOOKED_BLOCK_FACE = defineSynchedEntityData<GrappleFishingHook, _>(OmniCrossbowEntityDataSerializers.OPTIONAL_DIRECTION)
        val PULLING_OWNER = defineSynchedEntityData<GrappleFishingHook, Boolean>(EntityDataSerializers.BOOLEAN)

        const val MAX_PULL_SPEED = 1.0
        const val MIN_DISTANCE = 1.5
        const val MAX_DISTANCE = 64.0
        const val PULL_FACTOR = 0.5 // exponential
        const val KNOCKBACK_RESISTANCE_WEIGHT_FACTOR = 200f // Max knockback resistance is 0.4 with full netherite armor
        const val NONLIVING_ENTITY_VOLUME_FACTOR = 20f
        const val GRAPPLING_ENTITY_AIR_RESISTANCE = 0.9f
        const val DISCONNECT_BOOST = 0.7
        const val NO_HIT_TICKS = 6

        fun getWeightScore(entity: Entity): Float {
            if (entity isIn OmniCrossbowTags.GRAPPLE_UNMOVEABLE) return Float.MAX_VALUE

            if (entity !is LivingEntity) return with (entity) {
                bbWidth * bbWidth * bbHeight * NONLIVING_ENTITY_VOLUME_FACTOR
            }

            return entity.getAttributeBaseValue(Attributes.MAX_HEALTH).toFloat() +
                    if (entity is Player)
                        entity.getAttributeValue(KNOCKBACK_RESISTANCE).toFloat() * KNOCKBACK_RESISTANCE_WEIGHT_FACTOR
                    else 0f
        }

        @JvmStatic
        fun isBeingPulled(entity: Entity) =
            entity.getAttached(OmniCrossbowAttachments.CONNECTED_GRAPPLE_HOOKS)?.any { it.isPulling && (it.hookedBlockFace != null || it.pullingOwner == (it.getOwner() == entity)) } == true
    }
}
package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.registry.OmniCrossbowDamageTypes
import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import archives.tater.omnicrossbow.util.*
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth.ceil
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.monster.CrossbowAttackMob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.CollisionContext
import org.joml.Quaternionf
import org.joml.Quaternionfc
import java.util.*

class BeaconLaser(type: EntityType<out BeaconLaser>, level: Level) : Entity(type, level) {
    private var ticksRemaining: Int = DURATION
    private var animationTime: Int = 0
    private var thickness = 0f

    private var ownerRef by OWNER
    var distance by DISTANCE
    var active by ACTIVE
    var angleOffset by ANGLE_OFFSET

    var owner by ::ownerRef

    constructor(level: Level, owner: LivingEntity, offset: Quaternionfc) : this(OmniCrossbowEntities.BEACON_LASER, level) {
        this.owner = owner
        this.angleOffset = offset
        updatePosition(owner)
    }

    constructor(level: Level, owner: LivingEntity, direction: Vec3)
        : this(level, owner, Quaternionf().rotateTo((owner as? CrossbowAttackMob)?.target?.let {
            (it.position() - owner.position()).normalize().toVector3f()
        } ?: owner.lookAngle.toVector3f(), direction.normalize().toVector3f()))

    fun getAnimationTime(partialTicks: Float) = animationTime + partialTicks

    fun getThickness(partialTicks: Float) = (thickness + partialTicks * (if (active) TRANSITION_STEP else -TRANSITION_STEP)).coerceIn(0f, 1f)

    override fun defineSynchedData(entityData: SynchedEntityData.Builder) {
        entityData.define(OWNER, Optional.empty())
        entityData.define(ACTIVE, true)
        entityData.define(DISTANCE, 0)
        entityData.define(ANGLE_OFFSET, Quaternionf())
    }

    private fun updatePosition(owner: LivingEntity) {
        if (owner is Mob && owner is CrossbowAttackMob) {
            val target = owner.target
            if (target != null)
                owner.lookAt(target, MOB_TURN_SPEED, MOB_TURN_SPEED)
        }

        val angle = owner.lookAngle.toVector3f().rotate(angleOffset)
        lookAtAngle(angle)

        setPos(owner.eyePosition.subtract(0.0, EYE_MARGIN, 0.0).add(lookAngle * OFFSET))
    }

    override fun tick() {
        val owner = owner ?: run {
            discard()
            return
        }

        updatePosition(owner)

        if (active && (owner !is Player || !owner.abilities.flying))
            owner.deltaMovement += lookAngle * -RECOIL

        val level = level()

        if (level is ServerLevel) {

            if (active) {
                val start = position()
                val hit = level.clip(
                    ClipContext(
                        start,
                        start + lookAngle * MAX_DISTANCE,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        CollisionContext.empty()
                    )
                )
                distance = ceil((hit.location - start).length())
                for (target in getEntitiesPierced(level, start, hit.location, 0.2, owner)) {
                    target.hurtServer(
                        level,
                        owner.damageSources().source(OmniCrossbowDamageTypes.BEACON, owner),
                        (((target as? LivingEntity)?.maxHealth ?: 20f) / DURATION)
                            .coerceAtLeast(1f)
                            .let {
                                if (target isIn OmniCrossbowTags.UNCAPPED_BEACON_DAMAGE) it else it.coerceAtMost(
                                    MAX_DAMAGE / DURATION
                                )
                            }
                            .let { if (target isIn OmniCrossbowTags.EXTRA_BEACON_DAMAGE) 1.1f * it else it }
                    )
                }
            }

            ticksRemaining--
            if (active && ticksRemaining <= 0)
                active = false
            if (ticksRemaining <= -TRANSITION_TICKS) {
                discard()
                return
            }

        } else { // Client

            animationTime++
            if (active) {
                if (thickness < 1f)
                    thickness = (thickness + 1f / TRANSITION_TICKS).coerceAtMost(1f)
            } else {
                if (thickness > 0f)
                    thickness -= 1f / TRANSITION_TICKS
                if (thickness <= 0f) {
                    discard()
                    return
                }
            }
        }
    }

    override fun hurtServer(level: ServerLevel, source: DamageSource, damage: Float): Boolean = false

    override fun readAdditionalSaveData(input: ValueInput) {}

    override fun addAdditionalSaveData(output: ValueOutput) {}

    companion object {
        const val MAX_DISTANCE = 64.0
        const val DURATION = 40
        const val MAX_DAMAGE = 100f
        const val TRANSITION_TICKS = 10
        const val TRANSITION_STEP = 1f / TRANSITION_TICKS
        const val EYE_MARGIN = 0.4
        const val MOB_TURN_SPEED = 2f
        const val RECOIL = 0.06
        const val OFFSET = 0.5

        val OWNER: EntityDataAccessor<Optional<EntityReference<LivingEntity>>> =
            SynchedEntityData.defineId(BeaconLaser::class.java, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE)

        val DISTANCE: EntityDataAccessor<Int> = SynchedEntityData.defineId(BeaconLaser::class.java, EntityDataSerializers.INT)

        val ACTIVE: EntityDataAccessor<Boolean> = SynchedEntityData.defineId(BeaconLaser::class.java, EntityDataSerializers.BOOLEAN)

        val ANGLE_OFFSET: EntityDataAccessor<Quaternionfc> = SynchedEntityData.defineId(BeaconLaser::class.java, EntityDataSerializers.QUATERNION)
    }
}
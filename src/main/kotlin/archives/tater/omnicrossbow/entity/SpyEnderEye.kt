package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.network.SpyEyeInputPayload
import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import archives.tater.omnicrossbow.util.lookAtAngle
import archives.tater.omnicrossbow.util.plus
import archives.tater.omnicrossbow.util.times
import net.fabricmc.fabric.api.entity.FakePlayer
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import com.mojang.authlib.GameProfile
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ChunkTrackingView
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.*
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.GameType
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.Vec3
import java.util.*

class SpyEnderEye(type: EntityType<out SpyEnderEye>, level: Level) : Entity(type, level), ItemSupplier {

    var owner: ServerPlayer? = null
        private set
    var fakePlayer: EyeFakePlayer? = null
        private set

    var input: Vec3 = Vec3.ZERO
        set(value) {
            field = value
            if (value.lengthSqr() != 0.0)
                lookAtAngle(value)
        }

    private val item = Items.ENDER_EYE.defaultInstance

    constructor(player: ServerPlayer) : this(OmniCrossbowEntities.SPY_ENDER_EYE, player.level()) {
        setPos(player.x, player.eyeY - 0.1, player.z)
        owner = player
        fakePlayer = EyeFakePlayer(player)
    }

    override fun tick() {
        super.tick()

        deltaMovement *= FRICTION
        deltaMovement += input * MOVEMENT_SPEED
        move(MoverType.SELF, deltaMovement)

        val level = level()

        if (level is ServerLevel) {
            val owner = owner
            if (owner == null || owner.isRemoved || owner.level() != level || owner.isCrouching) {
                playSound(SoundEvents.ENDER_EYE_DEATH)
                spawnAtLocation(level, item)
                discard()
                return
            }
            fakePlayer?.apply {
                setOldPosAndRot()
                setPos(this@SpyEnderEye.position())
                level.chunkSource.move(this)
            }
        } else {
            val movement = deltaMovement
            level.addParticle(
                ParticleTypes.PORTAL,
                x + this.random.nextDouble() * 0.6 - 0.3,
                y - 0.5,
                z + this.random.nextDouble() * 0.6 - 0.3,
                movement.x,
                movement.y,
                movement.z
            )
        }
    }

    override fun onRemoval(reason: RemovalReason) {
        owner?.chunkTrackingView = ChunkTrackingView.EMPTY
        val fakePlayer = fakePlayer ?: return
        (level() as? ServerLevel)?.removePlayerImmediately(fakePlayer, RemovalReason.DISCARDED)
    }

    override fun getItem(): ItemStack = item

    override fun defineSynchedData(entityData: SynchedEntityData.Builder) {

    }

    override fun hurtServer(level: ServerLevel, source: DamageSource, damage: Float): Boolean = false

    override fun readAdditionalSaveData(input: ValueInput) {

    }

    override fun addAdditionalSaveData(output: ValueOutput) {

    }

    inner class EyeFakePlayer(owner: ServerPlayer) : FakePlayer(owner.level(), GameProfile(UUID.randomUUID(), "Spy Eye of ${owner.displayName?.string}")) {
        val eyeEntity = this@SpyEnderEye

        init {
            refreshDimensions()
            setGameMode(GameType.SPECTATOR)
        }

        override fun requestedViewDistance(): Int = eyeEntity.owner?.requestedViewDistance() ?: 2

        override fun getDefaultDimensions(pose: Pose): EntityDimensions = EntityDimensions.fixed(0f, 0f)
    }

    companion object : ServerPlayNetworking.PlayPayloadHandler<SpyEyeInputPayload> {
        const val FRICTION = 0.95
        const val MOVEMENT_SPEED = 0.025

        override fun receive(
            payload: SpyEyeInputPayload,
            context: ServerPlayNetworking.Context
        ) {
            val player = context.player()
            val eye = player.level().getEntities(OmniCrossbowEntities.SPY_ENDER_EYE) { it.owner == player }.firstOrNull() ?: return
            eye.input = payload.input.normalize()
        }
    }
}
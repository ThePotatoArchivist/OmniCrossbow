package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.EntityNullFix
import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import net.fabricmc.fabric.api.entity.FakePlayer
import com.mojang.authlib.GameProfile
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ChunkTrackingView
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.projectile.ItemSupplier
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class SpyEnderEye(type: EntityType<out SpyEnderEye>, level: Level) : EntityNullFix(type, level), ItemSupplier {

    var owner: ServerPlayer? = null
        private set
    var fakePlayer: EyeFakePlayer? = null
        private set

    private val item = Items.ENDER_EYE.defaultInstance

    constructor(player: ServerPlayer) : this(OmniCrossbowEntities.SPY_ENDER_EYE, player.level()) {
        owner = player
        val fakePlayer = EyeFakePlayer(player)
        this.fakePlayer = fakePlayer
        player.level().addNewPlayer(fakePlayer)
    }

    override fun tick() {
        super.tick()
        val level = level()

        if (level is ServerLevel)
            fakePlayer?.apply {
                setOldPosAndRot()
                setPos(this@SpyEnderEye.position())
                level.chunkSource.move(this)
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

    inner class EyeFakePlayer(owner: ServerPlayer) : FakePlayer(owner.level(), GameProfile(owner.uuid, "Spy Eye of ${owner.displayName!!.string}")) {
        val eyeEntity = this@SpyEnderEye

        init {
            refreshDimensions()
        }

        override fun requestedViewDistance(): Int = eyeEntity.owner?.requestedViewDistance() ?: 2

        override fun getDefaultDimensions(pose: Pose): EntityDimensions = EntityDimensions.fixed(0f, 0f)
    }
}
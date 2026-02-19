package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.entity.BeaconLaser
import archives.tater.omnicrossbow.entity.GrappleFishingHook
import archives.tater.omnicrossbow.entity.SpyEnderEye
import archives.tater.omnicrossbow.mixin.behavior.access.BoatItemAccessor
import archives.tater.omnicrossbow.mixin.behavior.access.MinecartItemAccessor
import archives.tater.omnicrossbow.mixin.behavior.access.MobBucketItemAccessor
import archives.tater.omnicrossbow.network.ViewSpyEyePayload
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.*
import archives.tater.omnicrossbow.util.get
import archives.tater.omnicrossbow.util.lookAtAngle
import archives.tater.omnicrossbow.util.unaryMinus
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponents
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity


object OmniCrossbowProjectileActions {
    private fun register(path: String, codec: MapCodec<out ProjectileAction.Inline>) {
        Registry.register(OmniCrossbowBuiltinRegistries.PROJECTILE_ACTION_TYPE, OmniCrossbow.id(path), codec)
    }

    private fun <T: ProjectileAction> register(path: String, action: T): T =
        Registry.register(OmniCrossbowBuiltinRegistries.PROJECTILE_ACTION, OmniCrossbow.id(path), action)

    private fun registerDelegated(path: String, action: Delegated) = register(path, action)

    private fun registerProjectile(path: String, action: SpawnProjectile<*>) = register(path, action)

    private fun <E: Entity> registerEntity(path: String, action: SpawnEntity<E>) = register(path, action)

    @JvmField
    val NONE = registerDelegated("none") { _, _, _, _, _, _ -> }

    @JvmField
    val SPAWN_BOAT = registerEntity("spawn_entity/boat") {
        (it.item as? BoatItemAccessor)?.entityType
    }

    @JvmField
    val SPAWN_MINECART = registerEntity("spawn_entity/minecart") {
        (it.item as? MinecartItemAccessor)?.type
    }

    @JvmField
    val FROM_ENTITY_DATA = registerEntity("spawn_entity/from_entity_data") {
        it[DataComponents.ENTITY_DATA]?.type()
    }

    @JvmField
    val FROM_BUCKET = registerEntity("spawn_entity/from_bucket") {
        (it.item as? MobBucketItemAccessor)?.type
    }

    @JvmField
    val BEACON_LASER = registerDelegated("beacon_laser") { _, velocity, level, shooter, _, _ ->
        level.addFreshEntity(BeaconLaser(level, shooter, velocity))
    }

    @JvmField
    val SPY_ENDER_EYE = registerDelegated("spy_ender_eye") { _, velocity, level, shooter, _, _ ->
        if (shooter is ServerPlayer)
            SpyEnderEye(shooter).let {
                it.deltaMovement = velocity
                it.lookAtAngle(-velocity)
                level.addFreshEntity(it)
                ServerPlayNetworking.send(shooter, ViewSpyEyePayload(it.id))
                it.fakePlayer?.let(level::addNewPlayer)
            }
    }

    @JvmField
    val GRAPPLE_FISHING_HOOK = registerProjectile("grapple_fishing_hook") { level, shooter, _, projectile ->
        val disconnected = shooter[OmniCrossbowAttachments.CONNECTED_GRAPPLE_HOOKS]
            ?.mapNotNull { if (it.owner == shooter) it else null }

        if (disconnected?.isEmpty() == false) {
            for (hook in disconnected)
                hook.discard()
            return@registerProjectile null
        }

        projectile.hurtAndBreak(1, level, shooter as? ServerPlayer) {
            level.sendParticles(ItemParticleOption(ParticleTypes.ITEM, it.defaultInstance), shooter.x, shooter.eyeY - 0.1, shooter.z, 8, 0.0, 0.0, 0.0, 0.1)
        }

        GrappleFishingHook(level, shooter, projectile)
    }

    fun init() {
        register("default", ProjectileAction.Default)
        register("spawn_projectile", SpawnProjectile.Direct.CODEC)
        register("spawn_projectile/wind_charge", SpawnProjectile.CustomWindCharge.CODEC)
        register("spawn_entity", SpawnEntity.Direct.CODEC)
        register("spawn_entity/falling_block", SpawnEntity.FallingBlock.CODEC)
        registerEntity("spawn_entity/item", SpawnEntity.Item)
        register("fire_beam", FireBeam.CODEC)
        register("pierce", Pierce.CODEC)
        register("projectile_spray", ProjectileSpray.CODEC)
    }
}
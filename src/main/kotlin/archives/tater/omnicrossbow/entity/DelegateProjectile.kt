package archives.tater.omnicrossbow.entity

import archives.tater.omnicrossbow.registry.OmniCrossbowEntities
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.level.Level

class DelegateProjectile(type: EntityType<out DelegateProjectile>, level: Level) : Projectile(type, level) {
    override fun defineSynchedData(entityData: SynchedEntityData.Builder) {}

    constructor(level: Level, shooter: LivingEntity) : this(OmniCrossbowEntities.DELEGATE_PROJECTILE, level) {
        setPos(shooter.x, shooter.eyeY - 0.1, shooter.z)
    }
}
package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.valueproviders.IntProvider
import net.minecraft.util.valueproviders.IntProviders
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

@ConsistentCopyVisibility
@JvmRecord
data class ProjectileSpray private constructor(
    val type: EntityType<*>,
    val count: IntProvider,
    val uncertainty: Float,
) : Delegated, ProjectileAction.Inline {

    override fun shoot(
        pos: Vec3,
        velocity: Vec3,
        level: ServerLevel,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack
    ) {
        val direction = velocity.normalize()
        val power = velocity.length().toFloat()

        repeat(count.sample(shooter.random)) {
            (type.create(level, EntitySpawnReason.TRIGGERED) as? Projectile)?.apply {
                setPos(pos)
                owner = shooter
            }?.let { entity ->
                Projectile.spawnProjectile(entity, level, projectile) {
                    it.shoot(direction.x, direction.y, direction.z, power, uncertainty)
                }
            }
        }
    }

    override val codec: MapCodec<out ProjectileAction.Inline> get() = CODEC

    companion object {
        val CODEC: MapCodec<ProjectileSpray> = RecordCodecBuilder.mapCodec { it.group(
            EntityType.CODEC.fieldOf("entity").forGetter(ProjectileSpray::type),
            IntProviders.NON_NEGATIVE_CODEC.fieldOf("count").forGetter(ProjectileSpray::count),
            ExtraCodecs.NON_NEGATIVE_FLOAT.fieldOf("uncertainty").forGetter(ProjectileSpray::uncertainty)
        ).apply(it, ::ProjectileSpray) }

        fun of(type: EntityType<out Projectile>, count: IntProvider, uncertainty: Float) = ProjectileSpray(type, count, uncertainty)

        operator fun invoke(type: EntityType<out Projectile>, count: IntProvider, uncertainty: Float) = of(type, count, uncertainty)
    }
}
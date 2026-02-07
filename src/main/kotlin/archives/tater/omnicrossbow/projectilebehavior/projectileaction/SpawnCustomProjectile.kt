package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.projectilebehavior.impactaction.ImpactAction
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

@JvmRecord
data class SpawnCustomProjectile(
    val impactAction: ImpactAction = ImpactAction.None,
) : SpawnProjectile<CustomItemProjectile> {

    override fun createProjectile(
        level: Level,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack
    ) = CustomItemProjectile(shooter, level, projectile, impactAction)

    override val codec: MapCodec<out ProjectileAction> get() = CODEC

    companion object {
        val CODEC: MapCodec<SpawnCustomProjectile> = RecordCodecBuilder.mapCodec { it.group(
            ImpactAction.CODEC.optionalFieldOf("impact_action", ImpactAction.None).forGetter(SpawnCustomProjectile::impactAction),
        ).apply(it, ::SpawnCustomProjectile) }
    }
}
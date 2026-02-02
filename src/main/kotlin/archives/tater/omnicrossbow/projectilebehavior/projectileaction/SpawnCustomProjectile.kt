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
    val impactActions: List<ImpactAction>,
) : SpawnProjectile<CustomItemProjectile> {
    override fun createProjectile(
        level: Level,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack
    ) = CustomItemProjectile(shooter, level, projectile, impactActions).apply {

    }

    override val codec: MapCodec<out ProjectileAction> get() = CODEC

    companion object {
        val CODEC: MapCodec<SpawnCustomProjectile> = RecordCodecBuilder.mapCodec { it.group(
            ImpactAction.CODEC.listOf().fieldOf("impact_actions").forGetter(SpawnCustomProjectile::impactActions)
        ).apply(it, ::SpawnCustomProjectile) }
    }
}
package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.projectilebehavior.impactaction.ImpactAction
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

@JvmRecord
data class SpawnCustomProjectile(
    val impactBlock: ImpactAction<BlockHitResult> = ImpactAction.None,
    val impactEntity: ImpactAction<EntityHitResult> = ImpactAction.None,
) : SpawnProjectile<CustomItemProjectile> {

    override fun createProjectile(
        level: Level,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack
    ) = CustomItemProjectile(shooter, level, projectile, impactBlock, impactEntity)

    override val codec: MapCodec<out ProjectileAction> get() = CODEC

    companion object {
        val CODEC: MapCodec<SpawnCustomProjectile> = RecordCodecBuilder.mapCodec { it.group(
            ImpactAction.BLOCK_CODEC.optionalFieldOf("impact_block", ImpactAction.None).forGetter(SpawnCustomProjectile::impactBlock),
            ImpactAction.ENTITY_CODEC.optionalFieldOf("impact_entity", ImpactAction.None).forGetter(SpawnCustomProjectile::impactEntity),
        ).apply(it, ::SpawnCustomProjectile) }
    }
}
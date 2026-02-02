package archives.tater.omnicrossbow.projectilebehavior.action

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import com.mojang.serialization.MapCodec
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

@JvmRecord
data class SpawnCustomProjectile(
    val a: Nothing = TODO()
) : SpawnProjectile<CustomItemProjectile> {
    override fun createProjectile(
        level: Level,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack
    ) = CustomItemProjectile(shooter, level, projectile).apply {

    }

    override val codec: MapCodec<out ProjectileAction> get() = CODEC

    companion object {
        val CODEC: MapCodec<SpawnCustomProjectile> = TODO()
    }
}
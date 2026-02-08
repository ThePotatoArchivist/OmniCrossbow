package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.phys.Vec3

fun interface Delegated : ProjectileAction {
    fun shoot(
        pos: Vec3,
        velocity: Vec3,
        level: ServerLevel,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack
    )
}

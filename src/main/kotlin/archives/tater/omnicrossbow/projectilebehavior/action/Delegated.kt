package archives.tater.omnicrossbow.projectilebehavior.action

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

interface Delegated : ProjectileAction {
    fun shoot(
        pos: Vec3,
        velocity: Vec3,
        level: Level,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack
    )

    typealias ShootAction = (
        pos: Vec3,
        velocity: Vec3,
        level: Level,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack,
    ) -> Unit
}

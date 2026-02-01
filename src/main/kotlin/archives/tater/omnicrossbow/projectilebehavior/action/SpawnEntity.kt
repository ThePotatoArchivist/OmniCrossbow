package archives.tater.omnicrossbow.projectilebehavior.action

import com.mojang.serialization.MapCodec
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

@JvmRecord
data class SpawnEntity(val type: EntityType<*>) : Delegated {

    override fun shoot(
        pos: Vec3,
        velocity: Vec3,
        level: Level,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack
    ) {
        type.create(level, EntitySpawnReason.DISPENSER)?.apply {
            setPos(pos)
            deltaMovement = velocity
        }?.let { level.addFreshEntity(it) }
    }

    override val codec: MapCodec<out ProjectileAction> get() = CODEC

    companion object {
        val CODEC: MapCodec<SpawnEntity> = EntityType.CODEC.fieldOf("entity_type")
            .xmap(::SpawnEntity, SpawnEntity::type)
    }
}
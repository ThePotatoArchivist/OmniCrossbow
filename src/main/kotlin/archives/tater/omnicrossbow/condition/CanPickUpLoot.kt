package archives.tater.omnicrossbow.condition

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.minecraft.advancements.predicates.entity.EntitySubPredicate
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.phys.Vec3

data object CanPickUpLoot : EntitySubPredicate {
    override fun matches(entity: Entity, level: ServerLevel, position: Vec3?): Boolean =
        (entity as? LivingEntity)?.canPickUpLoot() == true

    val CODEC: Codec<CanPickUpLoot> = MapCodec.unitCodec(this)
}
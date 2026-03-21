package archives.tater.lockedloaded.condition

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.util.context.ContextKey
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.LootContext.EntityTarget
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition

@JvmRecord
data class CanPickUpLoot(val entity: EntityTarget) : LootItemCondition, LootItemCondition.Builder {
    override fun codec(): MapCodec<out LootItemCondition> = CODEC

    override fun test(context: LootContext): Boolean = (entity[context] as? LivingEntity)?.canPickUpLoot() == true

    override fun getReferencedContextParams(): Set<ContextKey<*>> = setOf(entity.contextParam())

    override fun build(): LootItemCondition = this

    companion object {
        val CODEC: MapCodec<CanPickUpLoot> = RecordCodecBuilder.mapCodec { it.group(
            EntityTarget.CODEC.fieldOf("entity").forGetter(CanPickUpLoot::entity)
        ).apply(it, ::CanPickUpLoot) }
    }
}
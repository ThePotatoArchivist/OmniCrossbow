package archives.tater.omnicrossbow.projectilebehavior.projectileaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.projectilebehavior.ImpactAction
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.ConditionalEffect
import net.minecraft.world.level.Level
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult
import java.util.*

@JvmRecord
data class SpawnCustomProjectile(
    val impactBlock: ImpactAction.Series<BlockHitResult> = listOf(),
    val impactEntity: ImpactAction.Series<EntityHitResult> = listOf(),
) : SpawnProjectile<CustomItemProjectile> {

    override fun createProjectile(
        level: Level,
        shooter: LivingEntity,
        weapon: ItemStack,
        projectile: ItemStack
    ) = CustomItemProjectile(shooter, level, projectile, impactBlock, impactEntity).apply {

    }

    override val codec: MapCodec<out ProjectileAction> get() = CODEC

    class Builder {
        private val impactBlock = mutableListOf<ConditionalEffect<ImpactAction<BlockHitResult>>>()
        private val impactEntity = mutableListOf<ConditionalEffect<ImpactAction<EntityHitResult>>>()

        @JvmName("addBlock")
        fun add(action: ImpactAction<BlockHitResult>) {
            impactBlock.add(ConditionalEffect(action, Optional.empty()))
        }

        @JvmName("addBlock")
        fun add(action: ImpactAction<BlockHitResult>, condition: LootItemCondition) {
            impactBlock.add(ConditionalEffect(action, Optional.of(condition)))
        }

        @JvmName("addEntity")
        fun add(action: ImpactAction<EntityHitResult>) {
            impactEntity.add(ConditionalEffect(action, Optional.empty()))
        }

        @JvmName("addEntity")
        fun add(action: ImpactAction<EntityHitResult>, condition: LootItemCondition) {
            impactEntity.add(ConditionalEffect(action, Optional.of(condition)))
        }

        @JvmName("addBoth")
        fun add(action: ImpactAction<HitResult>) {
            add(action as ImpactAction<BlockHitResult>)
            add(action as ImpactAction<EntityHitResult>)
        }

        @JvmName("addBoth")
        fun add(action: ImpactAction<HitResult>, condition: LootItemCondition) {
            add(action as ImpactAction<BlockHitResult>, condition)
            add(action as ImpactAction<EntityHitResult>, condition)
        }

        @JvmSynthetic
        @JvmName("plusBlock")
        operator fun ImpactAction<BlockHitResult>.unaryPlus() { add(this) }
        @JvmSynthetic
        @JvmName("plusEntity")
        operator fun ImpactAction<EntityHitResult>.unaryPlus() { add(this) }
        @JvmSynthetic
        @JvmName("plusBoth")
        operator fun ImpactAction<HitResult>.unaryPlus() { add(this) }

        fun build() = SpawnCustomProjectile(impactBlock, impactEntity)
    }

    companion object {
        val CODEC: MapCodec<SpawnCustomProjectile> = RecordCodecBuilder.mapCodec { it.group(
            ImpactAction.BLOCK_SERIES_CODEC.fieldOf("impact_block").forGetter(SpawnCustomProjectile::impactBlock),
            ImpactAction.ENTITY_SERIES_CODEC.fieldOf("impact_entity").forGetter(SpawnCustomProjectile::impactEntity),
        ).apply(it, ::SpawnCustomProjectile) }

        inline fun of(init: Builder.() -> Unit) = Builder().apply(init).build()

        inline operator fun invoke(init: Builder.() -> Unit) = of(init)
    }
}
package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.entity.DelegateFakePlayer
import archives.tater.omnicrossbow.mixin.behavior.access.LivingEntityInvoker
import archives.tater.omnicrossbow.mixin.behavior.access.PlayerInvoker
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.enchantment.EnchantmentHelper
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult


@JvmRecord
data class Damage(val baseAttackDamage: Float = 1f) : ImpactAction.Inline {

    /**
     * @see Player.attack
     */
    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean {
        val entity = (hit as? EntityHitResult)?.entity ?: return false

        val fakePlayer = DelegateFakePlayer.create(level, projectile)
        fakePlayer as PlayerInvoker
        fakePlayer as LivingEntityInvoker

        val owner = projectile.owner as? LivingEntity ?: fakePlayer
        val instance = fakePlayer.getAttribute(Attributes.ATTACK_DAMAGE) ?: return false
        instance.baseValue = baseAttackDamage.toDouble()

        val baseDamage = instance.value.toFloat()
        val attackingItemStack = projectile.item
        val damageSource = attackingItemStack.getDamageSource(owner) { level.damageSources().run { if (owner is Player) playerAttack(owner) else mobAttack(owner) } }
        val magicBoost = EnchantmentHelper.modifyDamage(level, attackingItemStack, entity, damageSource, baseDamage) - baseDamage

        if (baseDamage <= 0f && magicBoost <= 0f) return false

        val totalDamage = baseDamage + attackingItemStack.item.getAttackDamageBonus(entity, baseDamage, damageSource) + magicBoost

        val oldMovement = entity.deltaMovement
        if (!entity.hurtServer(level, damageSource, totalDamage)) return false

        fakePlayer.causeExtraKnockback(
            entity,
            fakePlayer.invokeGetKnockback(entity, damageSource),
            oldMovement
        )

        (owner as? PlayerInvoker ?: fakePlayer).invokeItemAttackInteraction(entity, attackingItemStack, damageSource, true)

//        fakePlayer.postPiercingAttack()

        return true
    }

    override val codec: MapCodec<out Damage> get() = CODEC

    companion object {
        val CODEC: MapCodec<Damage> = RecordCodecBuilder.mapCodec { it.group(
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("base_attack_damage", 1f).forGetter(Damage::baseAttackDamage)
        ).apply(it, ::Damage) }
    }
}
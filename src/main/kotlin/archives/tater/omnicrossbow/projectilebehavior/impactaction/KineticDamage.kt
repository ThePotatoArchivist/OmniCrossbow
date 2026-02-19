package archives.tater.omnicrossbow.projectilebehavior.impactaction

import archives.tater.omnicrossbow.entity.CustomItemProjectile
import archives.tater.omnicrossbow.entity.DelegateFakePlayer
import archives.tater.omnicrossbow.mixin.behavior.access.LivingEntityInvoker
import archives.tater.omnicrossbow.mixin.behavior.access.PlayerInvoker
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.component.DataComponents
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.Mth.floor
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.component.KineticWeapon.getMotion
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult


@JvmRecord
data class KineticDamage(val baseAttackDamage: Float = 1f) : ImpactAction.Inline {

    /**
     * @see net.minecraft.world.item.component.KineticWeapon.damageEntities
     * @see Player.stabAttack
     */
    @Suppress("CAST_NEVER_SUCCEEDS")
    override fun tryImpact(
        level: ServerLevel,
        projectile: CustomItemProjectile,
        hit: HitResult,
        originalItem: ItemStack
    ): Boolean {
        val entity = (hit as? EntityHitResult)?.entity ?: return false
        val attackingItemStack = projectile.item
        val kineticWeapon = attackingItemStack[DataComponents.KINETIC_WEAPON] ?: return false

        val fakePlayer = DelegateFakePlayer.create(level, projectile)
        fakePlayer as PlayerInvoker
        fakePlayer as LivingEntityInvoker

        val owner = projectile.owner as? LivingEntity ?: fakePlayer

        val instance = fakePlayer.getAttribute(Attributes.ATTACK_DAMAGE) ?: return false
        instance.baseValue = baseAttackDamage.toDouble()


        val attackerLookVector = projectile.deltaMovement.normalize()
        val attackerSpeedProjection: Double = attackerLookVector.dot(getMotion(projectile))

        owner.rememberStabbedEntity(entity)
        val targetSpeedProjection = attackerLookVector.dot(getMotion(entity))
        val relativeSpeed = (attackerSpeedProjection - targetSpeedProjection).coerceAtLeast(0.0)
        val dealsDismount = kineticWeapon.dismountConditions.isPresent && (kineticWeapon.dismountConditions.get()).test(0, attackerSpeedProjection, relativeSpeed, 1.0)
        val dealsKnockback = kineticWeapon.knockbackConditions.isPresent && (kineticWeapon.knockbackConditions.get()).test(0, attackerSpeedProjection, relativeSpeed, 1.0)
        val dealsDamage = kineticWeapon.damageConditions.isPresent && (kineticWeapon.damageConditions.get()).test(0, attackerSpeedProjection, relativeSpeed, 1.0)

        if (!dealsDismount && !dealsKnockback && !dealsDamage) return false

        val damageDealt = instance.value.toFloat() + floor(relativeSpeed * kineticWeapon.damageMultiplier)

        return fakePlayer.stabAttack(EquipmentSlot.MAINHAND, entity, damageDealt, dealsDamage, dealsKnockback, dealsDismount)
    }

    override val codec: MapCodec<out KineticDamage> get() = CODEC

    companion object {
        val CODEC: MapCodec<KineticDamage> = RecordCodecBuilder.mapCodec { it.group(
            ExtraCodecs.NON_NEGATIVE_FLOAT.optionalFieldOf("base_attack_damage", 1f).forGetter(KineticDamage::baseAttackDamage)
        ).apply(it, ::KineticDamage) }
    }
}
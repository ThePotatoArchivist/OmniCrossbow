package archives.tater.omnicrossbow.mixin.behavior;

import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.AbstractWindCharge;
import net.minecraft.world.entity.projectile.hurtingprojectile.windcharge.WindCharge;
import net.minecraft.world.level.Level;

@Mixin(WindCharge.class)
public abstract class WindChargeMixin extends AbstractWindCharge {
    public WindChargeMixin(EntityType<? extends AbstractWindCharge> type, Level level) {
        super(type, level);
    }

    @ModifyArg(
            method = "explode",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;explode(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;Lnet/minecraft/world/level/ExplosionDamageCalculator;DDDFZLnet/minecraft/world/level/Level$ExplosionInteraction;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/core/particles/ParticleOptions;Lnet/minecraft/util/random/WeightedList;Lnet/minecraft/core/Holder;)V"),
            index = 6
    )
    private float modifyExplosionRadius(float r) {
        return getAttachedOrElse(OmniCrossbowAttachments.WIND_CHARGE_EXPLOSION_RADIUS, r);
    }
}

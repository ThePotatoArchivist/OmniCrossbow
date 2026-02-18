package archives.tater.omnicrossbow.mixin.grapplehook;

import archives.tater.omnicrossbow.entity.GrappleFishingHook;
import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@SuppressWarnings("UnstableApiUsage")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @ModifyReturnValue(
            method = "getEffectiveGravity",
            at = @At("RETURN")
    )
    private double noGravityWhileGrappling(double original) {
        return GrappleFishingHook.isBeingPulled(this) ? 0.0 : original;
    }

    @ModifyExpressionValue(
            method = "travelInAir",
            at = {
                    @At(value = "CONSTANT", args = "floatValue=0.91"),
                    @At(value = "CONSTANT", args = "floatValue=0.98"),
            }
    )
    private float grapplingAirResistance(float original) {
        return GrappleFishingHook.isBeingPulled(this) ? GrappleFishingHook.GRAPPLING_ENTITY_AIR_RESISTANCE : original;
    }

    @ModifyReturnValue(
            method = "isInvulnerableTo",
            at = @At("RETURN")
    )
    private boolean noHitGrapple(boolean original, @Local(argsOnly = true) DamageSource source) {
        return original || source.getEntity() != null && source.getEntity().getAttachedOrElse(OmniCrossbowAttachments.GRAPPLE_NO_HIT_COOLDOWN, 0) > 0;
    }

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void tickNoHitCooldown(CallbackInfo ci) {
        if (!hasAttached(OmniCrossbowAttachments.GRAPPLE_NO_HIT_COOLDOWN)) return;
        int cooldown = getAttachedOrElse(OmniCrossbowAttachments.GRAPPLE_NO_HIT_COOLDOWN, 0);
        if (cooldown <= 0) {
            removeAttached(OmniCrossbowAttachments.GRAPPLE_NO_HIT_COOLDOWN);
            return;
        }
        setAttached(OmniCrossbowAttachments.GRAPPLE_NO_HIT_COOLDOWN, cooldown - 1);
    }
}

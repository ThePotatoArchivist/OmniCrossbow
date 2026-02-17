package archives.tater.omnicrossbow.mixin.client.grapplehook;

import archives.tater.omnicrossbow.entity.GrappleFishingHook;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

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
}

package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.duck.Grapplable;
import archives.tater.omnicrossbow.duck.Grappler;
import archives.tater.omnicrossbow.entity.GrappleFishingHookEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LivingEntity.class)
public class LivingEntityMixin implements Grappler {
    @Unique
    private @Nullable GrappleFishingHookEntity omnicrossbow$hook = null;

    @Override
    public @Nullable GrappleFishingHookEntity omnicrossbow$getHook() {
        return omnicrossbow$hook == null || omnicrossbow$hook.isRemoved() ? null : omnicrossbow$hook;
    }

    @Override
    public void omnicrossbow$setHook(@Nullable GrappleFishingHookEntity hook) {
        omnicrossbow$hook = hook;
    }

    @ModifyExpressionValue(
            method = "travel",
            at = @At(value = "CONSTANT", args = "doubleValue=0.9800000190734863")
    )
    private double sameDragReeling(double original) {
        return (omnicrossbow$hook != null && !omnicrossbow$hook.isRemoved() && omnicrossbow$hook.isPullingOwner()) || ((Grapplable) this).omnicrossbow$isGrappled() ? 0.91 : original;
    }
}

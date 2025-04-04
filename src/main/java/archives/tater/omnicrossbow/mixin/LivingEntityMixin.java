package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.OmniCrossbow;
import archives.tater.omnicrossbow.duck.Grapplable;
import archives.tater.omnicrossbow.duck.Grappler;
import archives.tater.omnicrossbow.entity.GrappleFishingHookEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Grappler {
    public LivingEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Unique
    private @Nullable GrappleFishingHookEntity omnicrossbow$hook = null;
    @Unique
    private boolean omnicrossbow$isOnWax = false;

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

    @Inject(
            method = "travel",
            at = @At("HEAD")
    )
    private void checkWax(Vec3d movementInput, CallbackInfo ci) {
        omnicrossbow$isOnWax = supportingBlockPos.map(pos -> getWorld().getBlockState(pos).isOf(OmniCrossbow.WAX_BLOCK)).orElse(false);
    }

    @ModifyExpressionValue(
            method = "applyMovementInput",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getMovementSpeed(F)F")
    )
    private float slowerOnWax(float original) {
        return omnicrossbow$isOnWax && !isSprinting() ? original * 0.1f : original;
    }

    @ModifyVariable(
            method = "travel",
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;getSlipperiness()F")
            ),
            at = @At("STORE"),
            ordinal = 1
    )
    private float noDragWax(float original) {
        return omnicrossbow$isOnWax ? 1f : original;
    }
}

package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.OmniCrossbow;
import archives.tater.omnicrossbow.block.WaxBlock;
import archives.tater.omnicrossbow.duck.Grapplable;
import archives.tater.omnicrossbow.duck.Grappler;
import archives.tater.omnicrossbow.duck.Slider;
import archives.tater.omnicrossbow.entity.GrappleFishingHookEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static java.lang.Math.max;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Grappler, Slider {
    @Shadow public abstract boolean hasStatusEffect(RegistryEntry<StatusEffect> effect);

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

    @SuppressWarnings("ConstantValue")
    @Inject(
            method = "travel",
            at = @At("HEAD")
    )
    private void checkWax(Vec3d movementInput, CallbackInfo ci) {
        omnicrossbow$isOnWax = isOnGround() && (!((Object) this instanceof PlayerEntity player) || !player.getAbilities().flying) && getBlockStateAtPos().isOf(OmniCrossbow.WAX_BLOCK);
    }

    @SuppressWarnings("ConstantValue")
    @Override
    public boolean omnicrossbow$canSlide() {
        return (omnicrossbow$isOnWax || hasStatusEffect(OmniCrossbow.WAXED_EFFECT)) && (!((Object) this instanceof PlayerEntity player) || !player.getAbilities().flying);
    }

    @Override
    public boolean omnicrossbow$shouldSlide() {
        return omnicrossbow$canSlide() && isSprinting();
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
        return omnicrossbow$canSlide() ? 1f : original;
    }

    @ModifyReturnValue(
            method = "getStepHeight",
            at = @At("RETURN")
    )
    private float slideAutoStep(float original) {
        return omnicrossbow$shouldSlide() ? max(original, 1) : original;
    }

    @SuppressWarnings("ConstantValue")
    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void spreadWax(CallbackInfo ci) {
        if (!getWorld().isClient && hasStatusEffect(OmniCrossbow.WAXED_EFFECT) && isOnGround() && (!((Object) this instanceof PlayerEntity player) || !player.getAbilities().flying) && !omnicrossbow$isOnWax) {
            WaxBlock.trySpread(getWorld(), getBlockPos(), Direction.DOWN, false, 1, 1);
        }
    }
}

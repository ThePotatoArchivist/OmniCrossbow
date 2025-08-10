package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.duck.Grapplable;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEntity.class)
public class MobEntityMixin {
    @Unique
    private int omnicrossbow$grappledTicks = 0;

    @Inject(
            method = "baseTick",
            at = @At("TAIL")
    )
    private void updateGrappledTicks(CallbackInfo ci) {
        if (((Grapplable) this).omnicrossbow$isGrappled())
            omnicrossbow$grappledTicks = 5;
        else if (omnicrossbow$grappledTicks > 0)
            omnicrossbow$grappledTicks--;
    }

    @Inject(
            method = "tryAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventAttackGrappled(Entity target, CallbackInfoReturnable<Boolean> cir) {
        if (omnicrossbow$grappledTicks > 0)
            cir.setReturnValue(false);
    }
}

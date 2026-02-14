package archives.tater.omnicrossbow.mixin.behavior;

import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @SuppressWarnings("UnstableApiUsage")
    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void tickDelay(CallbackInfo ci) {
        var delayedShotTracker = getAttached(OmniCrossbowAttachments.DELAYED_SHOTS);
        if (delayedShotTracker == null) return;
        delayedShotTracker.tick((LivingEntity) (Object) this);
    }
}

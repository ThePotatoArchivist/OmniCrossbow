package archives.tater.omnicrossbow.mixin.beacon;

import archives.tater.omnicrossbow.projectilebehavior.BeaconLaser;
import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
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

    @Inject(
            method = "tick",
            at = @At("TAIL")
    )
    private void tickBeacon(CallbackInfo ci) {
        if (!(level() instanceof ServerLevel serverLevel)) {
            BeaconLaser.tickClient((LivingEntity) (Object) this);
            return;
        }

        var beaconLaser = getAttached(OmniCrossbowAttachments.BEACON_LASER);
        if (beaconLaser == null) return;
        beaconLaser.tickServer(serverLevel, (LivingEntity) (Object) this);
    }
}

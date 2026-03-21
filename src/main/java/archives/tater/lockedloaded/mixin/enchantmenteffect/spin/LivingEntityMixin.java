package archives.tater.lockedloaded.mixin.enchantmenteffect.spin;

import archives.tater.lockedloaded.registry.LockedLoadedAttachments;
import archives.tater.lockedloaded.registry.LockedLoadedSounds;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

@SuppressWarnings("UnstableApiUsage")
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract boolean isUsingItem();

    public LivingEntityMixin(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Inject(
            method = "tick",
            at = @At("HEAD")
    )
    private void removeSpinning(CallbackInfo ci) {
        if (hasAttached(LockedLoadedAttachments.SPINNING_ITEM) && !isUsingItem()) {
            removeAttached(LockedLoadedAttachments.SPINNING_ITEM);
            if (level() instanceof ServerLevel serverLevel)
                serverLevel.getChunkSource().sendToTrackingPlayersAndSelf(this, new ClientboundStopSoundPacket(LockedLoadedSounds.SPIN.location(), getSoundSource()));
        }
    }
}

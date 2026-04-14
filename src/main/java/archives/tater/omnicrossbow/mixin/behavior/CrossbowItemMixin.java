package archives.tater.omnicrossbow.mixin.behavior;

import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior;
import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.objectweb.asm.Opcodes;

import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.level.Level;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @ModifyExpressionValue(
            method = "shootProjectile",
            at = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;CROSSBOW_SHOOT:Lnet/minecraft/sounds/SoundEvent;", opcode = Opcodes.GETSTATIC)
    )
    private SoundEvent replaceShootSound(SoundEvent original, @Local(argsOnly = true) Projectile projectile) {
        return projectile.getAttachedOrElse(OmniCrossbowAttachments.PROJECTILE_BEHAVIOR, ProjectileBehavior.DEFAULT)
                .shootSound()
                .map(Holder::value)
                .orElse(original);
    }

    @ModifyExpressionValue(
            method = "shootProjectile",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;sqrt(D)D")
    )
    private double ignoreGravityAiming(double original, @Local(argsOnly = true) Projectile projectile) {
        return projectile.getAttachedOrElse(OmniCrossbowAttachments.PROJECTILE_BEHAVIOR, ProjectileBehavior.DEFAULT).ignoreGravityAiming() ? 0.0 : original;
    }

    @Inject(
            method = "use",
            at = @At("HEAD"),
            cancellable = true
    )
    private void preventUseWithDelay(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        if (player.hasAttached(OmniCrossbowAttachments.DELAYED_SHOTS))
            cir.setReturnValue(InteractionResult.FAIL);
    }
}

package archives.tater.omnicrossbow.mixin.behavior;

import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.objectweb.asm.Opcodes;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
    @SuppressWarnings("UnstableApiUsage")
    @ModifyExpressionValue(
            method = "shootProjectile",
            at = @At(value = "FIELD", target = "Lnet/minecraft/sounds/SoundEvents;CROSSBOW_SHOOT:Lnet/minecraft/sounds/SoundEvent;", opcode = Opcodes.GETSTATIC)
    )
    private SoundEvent replaceShootSound(SoundEvent original, @Local(argsOnly = true) Projectile projectile) {
        var sound = projectile.getAttached(OmniCrossbowAttachments.SHOOT_SOUND);
        return sound == null ? original : sound.value();
    }
}

package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.entity.EmberEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.CrossbowUser;
import net.minecraft.entity.projectile.ProjectileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(CrossbowUser.class)
public interface CrossbowUserMixin {
    @ModifyArg(
            method = "shoot(Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/entity/projectile/ProjectileEntity;FF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;setVelocity(DDDFF)V"),
            index = 4
    )
    private float emberDivergence(float divergence, @Local(argsOnly = true) ProjectileEntity projectileEntity) {
        return projectileEntity instanceof EmberEntity ? 32f : divergence;
    }
}

package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.entity.EndCrystalProjectileEntity;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ThrownEntity.class)
public class ThrownEntityMixin {
    @SuppressWarnings("ConstantValue")
    @WrapWithCondition(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/thrown/ThrownEntity;setVelocity(Lnet/minecraft/util/math/Vec3d;)V")
    )
    private boolean endCrystalNoDrag(ThrownEntity instance, Vec3d vec3d) {
        return !((Object) this instanceof EndCrystalProjectileEntity);
    }
}

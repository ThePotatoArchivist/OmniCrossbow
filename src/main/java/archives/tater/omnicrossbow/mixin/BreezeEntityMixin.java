package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.entity.OmniCrossbowEntities;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.BreezeEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(BreezeEntity.class)
public class BreezeEntityMixin {
    @ModifyExpressionValue(
            method = "getProjectileDeflection",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/projectile/ProjectileEntity;getType()Lnet/minecraft/entity/EntityType;", ordinal = 1)
    )
    private EntityType<?> handleLargeWindCharge(EntityType<?> original) {
        return original == OmniCrossbowEntities.LARGE_WIND_CHARGE ? EntityType.WIND_CHARGE : original;
    }
}

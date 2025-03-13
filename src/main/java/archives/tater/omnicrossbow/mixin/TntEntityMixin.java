package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.duck.ReplacesExplosionBehavior;
import net.minecraft.entity.TntEntity;
import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TntEntity.class)
public class TntEntityMixin {
    @ModifyArg(
            method = "explode",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;createExplosion(Lnet/minecraft/entity/Entity;Lnet/minecraft/entity/damage/DamageSource;Lnet/minecraft/world/explosion/ExplosionBehavior;DDDFZLnet/minecraft/world/World$ExplosionSourceType;)Lnet/minecraft/world/explosion/Explosion;")
    )
    private @Nullable ExplosionBehavior replaceExplosionBehavior(@Nullable ExplosionBehavior original) {
        return this instanceof ReplacesExplosionBehavior provider ? provider.omni$getExplosionBehavior(original) : original;
    }
}

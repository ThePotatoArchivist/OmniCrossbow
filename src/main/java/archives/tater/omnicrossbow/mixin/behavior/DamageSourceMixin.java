package archives.tater.omnicrossbow.mixin.behavior;

import archives.tater.omnicrossbow.entity.DelegateFakePlayer;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

@Mixin(DamageSource.class)
public class DamageSourceMixin {
    @Definition(id = "causingEntity", field = "Lnet/minecraft/world/damagesource/DamageSource;causingEntity:Lnet/minecraft/world/entity/Entity;")
    @Expression("this.causingEntity = @(?)")
    @ModifyExpressionValue(
            method = "<init>(Lnet/minecraft/core/Holder;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At("MIXINEXTRAS:EXPRESSION")
    )
    private Entity handleDelegatedOwner(Entity original) {
        return original == null ? null : DelegateFakePlayer.getOriginalOwner(original);
    }
}

package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.OmniCrossbow;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.tag.TagKey;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @WrapOperation(
            method = "damage",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;isIn(Lnet/minecraft/registry/tag/TagKey;)Z"),
            slice = @Slice(
                    from = @At(value = "FIELD", target = "Lnet/minecraft/registry/tag/DamageTypeTags;IS_EXPLOSION:Lnet/minecraft/registry/tag/TagKey;"),
                    to = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;takeKnockback(DDD)V")
            )
    )
    private boolean checkNoKnockbackTag(DamageSource instance, TagKey<DamageType> tag, Operation<Boolean> original) {
        return original.call(instance, tag) || instance.isIn(OmniCrossbow.NO_KNOCKBACK);
    }
}

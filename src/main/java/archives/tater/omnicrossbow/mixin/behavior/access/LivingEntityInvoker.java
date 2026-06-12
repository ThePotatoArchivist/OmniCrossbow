package archives.tater.omnicrossbow.mixin.behavior.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
    @Invoker
    void invokeDetectEquipmentUpdates();

    @Accessor
    void setAttackStrengthTicker(int attackStrengthTicker);

    @Invoker
    float invokeGetKnockback(final Entity target, final DamageSource damageSource);
}

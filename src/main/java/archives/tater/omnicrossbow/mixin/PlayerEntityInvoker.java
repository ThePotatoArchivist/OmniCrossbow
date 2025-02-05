package archives.tater.omnicrossbow.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerEntity.class)
public interface PlayerEntityInvoker {
    @Invoker
    float invokeGetDamageAgainst(Entity target, float baseDamage, DamageSource damageSource);
}

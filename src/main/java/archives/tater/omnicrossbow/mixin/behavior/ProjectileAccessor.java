package archives.tater.omnicrossbow.mixin.behavior;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.projectile.Projectile;

@Mixin(Projectile.class)
public interface ProjectileAccessor {
    @Accessor
    void setLeftOwner(boolean leftOwner);
}

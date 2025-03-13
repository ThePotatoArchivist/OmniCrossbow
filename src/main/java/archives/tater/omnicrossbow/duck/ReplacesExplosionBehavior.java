package archives.tater.omnicrossbow.duck;

import net.minecraft.world.explosion.ExplosionBehavior;
import org.jetbrains.annotations.Nullable;

public interface ReplacesExplosionBehavior {
    @Nullable ExplosionBehavior omni$getExplosionBehavior(@Nullable ExplosionBehavior original);
}

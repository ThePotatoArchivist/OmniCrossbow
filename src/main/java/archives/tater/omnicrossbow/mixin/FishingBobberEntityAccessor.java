package archives.tater.omnicrossbow.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FishingBobberEntity.class)
public interface FishingBobberEntityAccessor {
    @Invoker
    void invokeCheckForCollision();

    @Invoker
    void invokeUpdateHookedEntityId(@Nullable Entity entity);
}

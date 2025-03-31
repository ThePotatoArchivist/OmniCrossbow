package archives.tater.omnicrossbow.mixin;

import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Accessor
    void setStandingEyeHeight(float standingEyeHeight);
    @Invoker
    Entity.MoveEffect invokeGetMoveEffect();
}

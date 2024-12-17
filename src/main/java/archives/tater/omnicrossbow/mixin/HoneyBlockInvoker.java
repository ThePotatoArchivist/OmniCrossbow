package archives.tater.omnicrossbow.mixin;

import net.minecraft.block.HoneyBlock;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HoneyBlock.class)
public interface HoneyBlockInvoker {
    @Invoker
    void invokeUpdateSlidingVelocity(Entity entity);
    @Invoker
    void invokeAddCollisionEffects(World world, Entity entity);
}

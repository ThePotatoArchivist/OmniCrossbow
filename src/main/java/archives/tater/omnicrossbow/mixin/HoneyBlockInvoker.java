package archives.tater.omnicrossbow.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.HoneyBlock;

@Mixin(HoneyBlock.class)
public interface HoneyBlockInvoker {
    @Invoker
    void invokeMaybeDoSlideAchievement(final Entity entity, final BlockPos pos);

    @Invoker
    void invokeDoSlideMovement(final Entity entity);

    @Invoker
    void invokeMaybeDoSlideEffects(final Level level, final Entity entity);
}

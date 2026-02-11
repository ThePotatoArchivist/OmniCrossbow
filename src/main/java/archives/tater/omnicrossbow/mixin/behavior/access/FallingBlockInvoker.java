package archives.tater.omnicrossbow.mixin.behavior.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.block.FallingBlock;

@Mixin(FallingBlock.class)
public interface FallingBlockInvoker {
    @Invoker
    void invokeFalling(FallingBlockEntity fallingBlockEntity);
}

package archives.tater.omnicrossbow.mixin.behavior.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(FireBlock.class)
public interface FireBlockInvoker {
    @Invoker
    int invokeGetBurnOdds(BlockState state);
}

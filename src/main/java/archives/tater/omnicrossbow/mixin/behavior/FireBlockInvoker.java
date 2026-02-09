package archives.tater.omnicrossbow.mixin.behavior;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(FireBlock.class)
public interface FireBlockInvoker {
    @Invoker
    int invokeGetBurnOdds(BlockState state);

    @Invoker
    BlockState invokeGetStateForPlacement(final BlockGetter level, final BlockPos pos);
}

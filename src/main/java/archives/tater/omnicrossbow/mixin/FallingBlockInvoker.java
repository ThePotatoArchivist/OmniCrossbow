package archives.tater.omnicrossbow.mixin;

import net.minecraft.block.FallingBlock;
import net.minecraft.entity.FallingBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(FallingBlock.class)
public interface FallingBlockInvoker {
    @Invoker
    void invokeConfigureFallingBlockEntity(FallingBlockEntity entity);
}

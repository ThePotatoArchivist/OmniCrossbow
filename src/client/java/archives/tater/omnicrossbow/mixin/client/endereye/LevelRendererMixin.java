package archives.tater.omnicrossbow.mixin.client.endereye;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @ModifyExpressionValue(
            method = "extractVisibleEntities",
            at = @At(value = "CONSTANT", args = "classValue=net/minecraft/client/player/LocalPlayer")
    )
    private boolean ignoreLocalPlayerCheck(boolean original) {
        return false;
    }
}

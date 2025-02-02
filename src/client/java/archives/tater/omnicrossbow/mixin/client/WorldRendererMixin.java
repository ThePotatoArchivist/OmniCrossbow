package archives.tater.omnicrossbow.mixin.client;

import archives.tater.omnicrossbow.entity.SpyEnderEyeEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow @Final private MinecraftClient client;

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/Camera;getFocusedEntity()Lnet/minecraft/entity/Entity;", ordinal = 3)
    )
    private Entity renderPlayer(Entity original) {
        return original instanceof SpyEnderEyeEntity ? client.player : original;
    }

    @ModifyReturnValue(
            method = "hasBlindnessOrDarkness",
            at = @At("RETURN")
    )
    private boolean blindnessIfEnderEyeInBlock(boolean original, @Local(argsOnly = true) Camera camera) {
        return original || camera.getFocusedEntity() instanceof SpyEnderEyeEntity && camera.getFocusedEntity().isInsideWall();
    }
}

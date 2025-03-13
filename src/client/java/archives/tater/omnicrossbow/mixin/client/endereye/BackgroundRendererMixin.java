package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.entity.SpyEnderEyeEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

    @ModifyVariable(
            method = "render",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/BackgroundRenderer;red:F", ordinal = 0),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/GameRenderer;getNightVisionStrength(Lnet/minecraft/entity/LivingEntity;F)F")
            ),
            ordinal = 2
    )
    private static float enderEyeNightVision(float original, @Local(argsOnly = true) Camera camera) {
        return camera.getFocusedEntity() instanceof SpyEnderEyeEntity && !camera.getFocusedEntity().isInsideWall() ? 1f : original;
    }
}

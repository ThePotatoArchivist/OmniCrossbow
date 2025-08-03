package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.entity.SpyEnderEyeEntity;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
    @Shadow private static float green;

    @Shadow private static float blue;

    @Shadow private static float red;

    @Inject(
            method = "applyFog",
            at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/BackgroundRenderer$FogData;fogStart:F", opcode = Opcodes.GETFIELD)
    )
    private static void fogIfEnderEyeInBlock(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci, @Local BackgroundRenderer.FogData fogData) {
        if (!(camera.getFocusedEntity() instanceof SpyEnderEyeEntity) || !camera.getFocusedEntity().isInsideWall()) return;

        if (fogData.fogType == BackgroundRenderer.FogType.FOG_SKY) {
            fogData.fogStart = 0.0F;
            fogData.fogEnd = 5f * 0.8F;
        } else {
            fogData.fogStart = 5f * 0.25F;
            fogData.fogEnd = 5f;
        }
    }

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

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/BackgroundRenderer;getFogModifier(Lnet/minecraft/entity/Entity;F)Lnet/minecraft/client/render/BackgroundRenderer$StatusEffectFogModifier;")
    )
    private static void modifyFogColor(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
        if (camera.getFocusedEntity() instanceof SpyEnderEyeEntity && camera.getFocusedEntity().isInsideWall()) {
            red = 0f;
            green = 0f;
            blue = 0f;
        }
    }
}

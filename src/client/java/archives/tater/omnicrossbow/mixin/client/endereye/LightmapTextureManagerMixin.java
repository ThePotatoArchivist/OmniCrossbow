package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.entity.SpyEnderEyeEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Shadow @Final private MinecraftClient client;

    @ModifyExpressionValue(
            method = "update",
            at = @At(value = "CONSTANT", args = "floatValue=0.0", ordinal = 1)
    )
    private float nightVisionEnderEye(float value) {
        return value == 0.0 && client.cameraEntity instanceof SpyEnderEyeEntity ? 1 : value;
    }
}

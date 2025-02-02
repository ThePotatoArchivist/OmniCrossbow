package archives.tater.omnicrossbow.mixin.client;

import archives.tater.omnicrossbow.entity.SpyEnderEyeEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LightmapTextureManager.class)
public class LightmapTextureManagerMixin {
    @Shadow @Final private MinecraftClient client;

    @ModifyVariable(
            method = "update",
            at = @At(value = "INVOKE", target = "Lorg/joml/Vector3f;<init>(FFF)V", ordinal = 0),
            ordinal = 6,
            remap = false
    )
    private float nightVisionEnderEye(float value) {
        return value == 0.0 && client.cameraEntity instanceof SpyEnderEyeEntity ? 1 : value;
    }


}

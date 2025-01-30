package archives.tater.omnicrossbow.mixin.client;

import archives.tater.omnicrossbow.OmniCrossbow;
import archives.tater.omnicrossbow.entity.SpyEnderEyeEntity;
import net.minecraft.client.gl.PostEffectProcessor;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Shadow @Nullable PostEffectProcessor postProcessor;

    @Shadow abstract void loadPostProcessor(Identifier id);

    @Inject(
            method = "onCameraEntitySet",
            at = @At("TAIL")
    )
    private void checkEnderEye(Entity entity, CallbackInfo ci) {
        if (postProcessor == null && entity instanceof SpyEnderEyeEntity)
            loadPostProcessor(new Identifier(OmniCrossbow.MOD_ID, "shaders/post/eye.json"));
    }
}

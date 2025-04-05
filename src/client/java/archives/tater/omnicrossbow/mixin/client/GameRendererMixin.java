package archives.tater.omnicrossbow.mixin.client;

import archives.tater.omnicrossbow.duck.Slider;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(
            method = "bobView",
            at = @At(value = "FIELD", target = "Lnet/minecraft/entity/player/PlayerEntity;horizontalSpeed:F", ordinal = 0),
            cancellable = true
    )
    private void noBobWhenSliding(MatrixStack matrices, float tickDelta, CallbackInfo ci, @Local PlayerEntity player) {
        if (((Slider) player).omnicrossbow$shouldSlide())
            ci.cancel();
    }
}

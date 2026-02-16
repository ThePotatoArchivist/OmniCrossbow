package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.OmniCrossbowClient;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.GameRenderer;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @ModifyExpressionValue(
            method = "renderItemInHand",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/CameraType;isFirstPerson()Z")
    )
    private boolean eyeNoItemRender(boolean original) {
        return original && OmniCrossbowClient.spyEye == null;
    }
}

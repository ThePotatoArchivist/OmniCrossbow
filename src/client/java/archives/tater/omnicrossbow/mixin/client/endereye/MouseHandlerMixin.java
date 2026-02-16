package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.OmniCrossbowClient;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MouseHandler;
import net.minecraft.client.player.LocalPlayer;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @WrapOperation(
            method = "turnPlayer",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;turn(DD)V")
    )
    private void turnEye(LocalPlayer instance, double xo, double yo, Operation<Void> original) {
        var spyEye = OmniCrossbowClient.spyEye;
        if (spyEye == null) {
            original.call(instance, xo, yo);
            return;
        }
        spyEye.turn(xo, yo);
    }
}

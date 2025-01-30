package archives.tater.omnicrossbow.mixin.client;

import archives.tater.omnicrossbow.entity.SpyEnderEyeEntity;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Mouse.class)
public class MouseMixin {
    @Shadow @Final private MinecraftClient client;

    @WrapOperation(
            method = "updateMouse",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V")
    )
    private void updateEnderEye(ClientPlayerEntity instance, double deltaX, double deltaY, Operation<Void> original) {
        if (client.getCameraEntity() instanceof SpyEnderEyeEntity spyEnderEye) {
            spyEnderEye.changeLookDirection(deltaX, deltaY);
        } else {
            original.call(instance, deltaX, deltaY);
        }
    }
}

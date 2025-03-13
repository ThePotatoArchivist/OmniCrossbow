package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.entity.SpyEnderEyeEntity;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {
    @Shadow @Final protected MinecraftClient client;

    @ModifyExpressionValue(
            method = "tickMovement",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;isCamera()Z")
    )
    private boolean checkEnderEye(boolean original) {
        return original || client.getCameraEntity() instanceof SpyEnderEyeEntity;
    }

    @ModifyReturnValue(
            method = "isCamera",
            at = @At("RETURN")
    )
    private boolean checkEnderEye2(boolean original) {
        return original || client.getCameraEntity() instanceof SpyEnderEyeEntity;
    }
}

package archives.tater.omnicrossbow.mixin.client;

import archives.tater.omnicrossbow.entity.SpyEnderEyeEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Shadow @Final private MinecraftClient client;

    @ModifyReturnValue(
            method = "getCameraPlayer",
            at = @At("RETURN")
    )
    private PlayerEntity checkEnderEye(PlayerEntity original) {
        return original == null && client.getCameraEntity() instanceof SpyEnderEyeEntity ? client.player : original;
    }
}

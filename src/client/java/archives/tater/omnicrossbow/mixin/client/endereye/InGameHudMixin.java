package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.entity.SpyEnderEyeEntity;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

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

    @Inject(
            method = "renderVignetteOverlay",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawTexture(Lnet/minecraft/util/Identifier;IIIFFIIII)V")
    )
    private void enderEyeVignette(DrawContext context, Entity entity, CallbackInfo ci) {
        if (!(MinecraftClient.getInstance().cameraEntity instanceof SpyEnderEyeEntity)) return;
        RenderSystem.blendFuncSeparate(
                GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ZERO
        );
        context.setShaderColor(0.12F, 0.68F, 0.43F, 1F);
    }
}

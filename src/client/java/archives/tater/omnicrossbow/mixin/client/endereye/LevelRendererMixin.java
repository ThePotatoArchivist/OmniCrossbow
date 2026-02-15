package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.OmniCrossbowClient;
import archives.tater.omnicrossbow.entity.SpyEnderEye;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;

import static java.util.Objects.requireNonNull;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @ModifyExpressionValue(
            method = "extractVisibleEntities",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;entity()Lnet/minecraft/world/entity/Entity;")
    )
    private Entity replaceCameraEntity(Entity original) {
        if (OmniCrossbowClient.spyEyeUuid == null) return original;
        var spyEye = requireNonNull(Minecraft.getInstance().level).getEntity(OmniCrossbowClient.spyEyeUuid);
        if (!(spyEye instanceof SpyEnderEye)) return original;
        return spyEye;
    }
}

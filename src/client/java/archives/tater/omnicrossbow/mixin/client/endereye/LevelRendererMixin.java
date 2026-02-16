package archives.tater.omnicrossbow.mixin.client.endereye;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.LevelRenderer;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
//    @WrapOperation(
//            method = "extractVisibleEntities",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;entity()Lnet/minecraft/world/entity/Entity;")
//    )
//    private Entity replaceCameraEntity(Camera instance, Operation<Entity> original) {
//        if (instance.isDetached() || OmniCrossbowClient.spyEyeUuid == null) return original.call(instance);
//        var spyEye = requireNonNull(Minecraft.getInstance().level).getEntity(OmniCrossbowClient.spyEyeUuid);
//        if (!(spyEye instanceof SpyEnderEye)) return original.call(instance);
//        return spyEye;
//    }

    @ModifyExpressionValue(
            method = "extractVisibleEntities",
            at = @At(value = "CONSTANT", args = "classValue=net/minecraft/client/player/LocalPlayer")
    )
    private boolean ignoreLocalPlayerCheck(boolean original) {
        return false;
    }
}

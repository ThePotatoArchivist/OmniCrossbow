package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.OmniCrossbowClient;
import archives.tater.omnicrossbow.entity.SpyEnderEye;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setPosition(Vec3 position);

    @Inject(
            method = "setup",
            at = @At(value = "INVOKE:FIRST", target = "Lnet/minecraft/world/entity/Entity;isPassenger()Z"),
            cancellable = true
    )
    private void spyEyeCamera(Level level, Entity entity, boolean detached, boolean mirror, float a, CallbackInfo ci) {
        if (OmniCrossbowClient.spyEyeUuid == null || Minecraft.getInstance().level == null) return;
        var spyEye = level.getEntity(OmniCrossbowClient.spyEyeUuid);
        if (!(spyEye instanceof SpyEnderEye spyEnderEye)) return;
        setPosition(spyEnderEye.getEyePosition(a));
        ci.cancel();
    }
}

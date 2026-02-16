package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.OmniCrossbowClient;
import archives.tater.omnicrossbow.entity.SpyEnderEye;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @ModifyVariable(
            method = "setup",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Entity modifyCameraEntity(Entity entity, @Local(argsOnly = true) Level level) {
        if (OmniCrossbowClient.spyEyeUuid == null || Minecraft.getInstance().level == null) return entity;
        var spyEye = level.getEntity(OmniCrossbowClient.spyEyeUuid);
        if (!(spyEye instanceof SpyEnderEye spyEnderEye)) return entity;
        return spyEnderEye;
    }
}

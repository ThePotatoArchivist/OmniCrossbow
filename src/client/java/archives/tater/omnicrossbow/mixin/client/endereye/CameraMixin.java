package archives.tater.omnicrossbow.mixin.client.endereye;

import archives.tater.omnicrossbow.OmniCrossbowClient;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import static java.util.Objects.requireNonNullElse;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @ModifyVariable(
            method = "setup",
            at = @At("HEAD"),
            argsOnly = true
    )
    private Entity modifyCameraEntity(Entity entity, @Local(argsOnly = true) Level level) {
        return requireNonNullElse(OmniCrossbowClient.spyEye, entity);
    }
}

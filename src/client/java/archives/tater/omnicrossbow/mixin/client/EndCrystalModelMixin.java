package archives.tater.omnicrossbow.mixin.client;

import archives.tater.omnicrossbow.client.render.entity.EndCrystalProjectileRenderer;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.model.object.crystal.EndCrystalModel;

@Mixin(EndCrystalModel.class)
public class EndCrystalModelMixin {
    @ModifyExpressionValue(
            method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/EndCrystalRenderState;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/EndCrystalRenderer;getY(F)F")
    )
    private float noBob(float original) {
        return (Object) this instanceof EndCrystalProjectileRenderer.Model ? 0 : original;
    }
}

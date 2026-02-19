package archives.tater.omnicrossbow.mixin.client;

import archives.tater.omnicrossbow.OmniCrossbowClient;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.item.ClientItem;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.resources.Identifier;

@Mixin(ModelBakery.class)
public class ModelBakeryMixin {
    @ModifyExpressionValue(
            method = "lambda$bakeModels$2",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/ClientItem;properties()Lnet/minecraft/client/renderer/item/ClientItem$Properties;")
    )
    private static ClientItem.Properties oversizeInGui(ClientItem.Properties original, @Local(argsOnly = true) Identifier id) {
        return OmniCrossbowClient.isCrossbow(id) ? new ClientItem.Properties(original.handAnimationOnSwap(), true, original.swapAnimationScale()) : original;
    }
}

package archives.tater.omnicrossbow.mixin.client;

import archives.tater.omnicrossbow.OmniCrossbowRenderer;
import net.minecraft.client.render.item.ItemModels;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedModelManager;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemModels.class)
public class ItemModelsMixin {
	@Shadow
	@Final
	private BakedModelManager modelManager;

	@Inject(
			method = "getModel(Lnet/minecraft/item/ItemStack;)Lnet/minecraft/client/render/model/BakedModel;",
			at = @At("HEAD"),
			cancellable = true
	)
	public void useDynamic(ItemStack stack, CallbackInfoReturnable<BakedModel> cir) {
		if (OmniCrossbowRenderer.useDynamic(stack))
            cir.setReturnValue(modelManager.getModel(OmniCrossbowRenderer.DYNAMIC_CROSSBOW));
	}
}

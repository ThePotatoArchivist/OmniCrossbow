package archives.tater.lockedloaded.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.renderer.item.properties.select.Charge;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ChargedProjectiles;

@Mixin(Charge.class)
public class ChargeMixin {
	@WrapOperation(
			method = "get(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/client/multiplayer/ClientLevel;Lnet/minecraft/world/entity/LivingEntity;ILnet/minecraft/world/item/ItemDisplayContext;)Lnet/minecraft/world/item/CrossbowItem$ChargeType;",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/component/ChargedProjectiles;contains(Lnet/minecraft/world/item/Item;)Z")
	)
	private boolean showFirstProjectile(ChargedProjectiles instance, Item item, Operation<Boolean> original) {
		return instance.items().getFirst().is(item);
	}
}
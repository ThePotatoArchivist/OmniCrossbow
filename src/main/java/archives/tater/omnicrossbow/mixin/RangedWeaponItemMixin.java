package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.MultichamberedEnchantment;
import archives.tater.omnicrossbow.OmniEnchantment;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(RangedWeaponItem.class)
public abstract class RangedWeaponItemMixin {
    @Shadow protected abstract int getWeaponStackDamage(ItemStack projectile);

    @Inject(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;createArrowEntity(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/projectile/ProjectileEntity;"),
            cancellable = true
    )
    private void customProjectile(ServerWorld world, LivingEntity shooter, Hand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target, CallbackInfo ci, @Local(ordinal = 1) ItemStack projectile, @Local int index) {
        if (!OmniEnchantment.shootProjectile(world, shooter, stack, projectile)) return;
        ci.cancel();
        if (OmniEnchantment.shouldUnloadImmediate(projectile)) {
            stack.damage(getWeaponStackDamage(projectile), shooter, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
            if (shooter.isOnGround() && shooter instanceof ServerPlayerEntity serverPlayer)
                serverPlayer.getItemCooldownManager().set(stack.getItem(), OmniEnchantment.getCooldown(projectile));
        }
    }

    @WrapOperation(
            method = "load",
            at = @At(value = "NEW", target = "(I)Ljava/util/ArrayList;")
    )
    private static ArrayList<ItemStack> useExistingList(int initialCapacity, Operation<ArrayList<ItemStack>> original, @Local(argsOnly = true, ordinal = 0) ItemStack crossbow) {
        if (MultichamberedEnchantment.hasMultichambered(crossbow)) {
            return new ArrayList<>(crossbow.getOrDefault(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT).getProjectiles());
        }
        return original.call(initialCapacity);
    }

    @ModifyVariable(
            method = "load",
            at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;<init>(I)V")
    )
    private static int modifyNumLoaded(int value, @Local(argsOnly = true, ordinal = 0) ItemStack crossbow) {
        return MultichamberedEnchantment.hasMultichambered(crossbow) ? 1 : value;
    }
}

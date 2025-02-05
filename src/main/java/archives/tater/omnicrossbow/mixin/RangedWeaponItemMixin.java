package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.OmniEnchantment;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(RangedWeaponItem.class)
public class RangedWeaponItemMixin {
    @Inject(
            method = "shootAll",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;createArrowEntity(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/entity/projectile/ProjectileEntity;"),
            cancellable = true
    )
    private void customProjectile(ServerWorld world, LivingEntity shooter, Hand hand, ItemStack stack, List<ItemStack> projectiles, float speed, float divergence, boolean critical, @Nullable LivingEntity target, CallbackInfo ci, @Local(ordinal = 1) ItemStack projectile, @Local int index) {
        if (!OmniEnchantment.shootProjectile(world, shooter, stack, projectile)) return;
        ci.cancel();
        if (OmniEnchantment.shouldUnloadImmediate(projectile))
            stack.damage(1, shooter, hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
        world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), OmniEnchantment.getSound(projectile), SoundCategory.PLAYERS, 1.0F, CrossbowItemInvoker.invokeGetSoundPitch(world.random, index));
    }

}

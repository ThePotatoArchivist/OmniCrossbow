package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.OmniEnchantment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    @Inject(
            method = "loadProjectile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;split(I)Lnet/minecraft/item/ItemStack;")
    )
    private static void giveRemainder(LivingEntity shooter, ItemStack crossbow, ItemStack projectile, boolean simulated, boolean creative, CallbackInfoReturnable<Boolean> cir) {
        var remainder = OmniEnchantment.getRemainder(projectile);
        if (remainder.isEmpty()) return;
        if (projectile.getCount() <= 1) {
            if (shooter.getStackInHand(Hand.MAIN_HAND) == projectile) {
                shooter.setStackInHand(Hand.MAIN_HAND, remainder);
                return;
            } else if (shooter.getStackInHand(Hand.OFF_HAND) == projectile) {
                shooter.setStackInHand(Hand.OFF_HAND, remainder);
                return;
            }
        }
        if (shooter instanceof PlayerEntity playerEntity)
            if (playerEntity.giveItemStack(remainder)) return;
        shooter.dropStack(remainder);
    }

    @Inject(
            method = "shoot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;createArrow(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;"),
            cancellable = true
    )
    private static void customProjectile(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated, CallbackInfo ci) {
        if (!OmniEnchantment.shootProjectile((ServerWorld) world, shooter, projectile)) return;
        ci.cancel();
        crossbow.damage(1, shooter, e -> e.sendToolBreakStatus(hand));
        world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.ITEM_CROSSBOW_SHOOT, SoundCategory.PLAYERS, 1.0F, soundPitch);
    }
}

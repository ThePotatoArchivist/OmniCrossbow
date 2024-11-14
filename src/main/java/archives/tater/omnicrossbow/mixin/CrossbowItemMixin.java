package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.OmniEnchantment;
import archives.tater.omnicrossbow.util.OmniUtil;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(CrossbowItem.class)
public abstract class CrossbowItemMixin {
    @Shadow
    private static List<ItemStack> getProjectiles(ItemStack crossbow) {
        throw new AssertionError();
    }

    @Inject(
            method = "shoot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;createArrow(Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;Lnet/minecraft/item/ItemStack;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/projectile/PersistentProjectileEntity;"),
            cancellable = true
    )
    private static void customProjectile(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated, CallbackInfo ci) {
        if (!OmniEnchantment.shootProjectile((ServerWorld) world, shooter, crossbow, projectile)) return;
        ci.cancel();
        crossbow.damage(1, shooter, e -> e.sendToolBreakStatus(hand));
        world.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), OmniEnchantment.getSound(projectile), SoundCategory.PLAYERS, 1.0F, soundPitch);
    }

    @WrapWithCondition(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;setCharged(Lnet/minecraft/item/ItemStack;Z)V")
    )
    private boolean preventUncharge(ItemStack stack, boolean charged) {
        return OmniEnchantment.shouldUnloadImmediate(OmniUtil.getMainProjectile(stack));
    }

    @WrapWithCondition(
            method = "postShoot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/CrossbowItem;clearProjectiles(Lnet/minecraft/item/ItemStack;)V")
    )
    private static boolean preventClear(ItemStack crossbow) {
        return OmniEnchantment.shouldUnloadImmediate(OmniUtil.getMainProjectile(crossbow));
    }

    @Inject(
            method = "postShoot",
            at = @At("HEAD")
    )
    private static void ejectRemainder(World world, LivingEntity entity, ItemStack stack, CallbackInfo ci) {
        if (world.isClient) return;
        for (var projectile : getProjectiles(stack)) {
            var remainder = OmniEnchantment.getRemainder(projectile);
            if (remainder.isEmpty()) continue;
            entity.dropStack(remainder, entity.getEyeHeight(entity.getPose()) - 0.1f);
        }
    }
}

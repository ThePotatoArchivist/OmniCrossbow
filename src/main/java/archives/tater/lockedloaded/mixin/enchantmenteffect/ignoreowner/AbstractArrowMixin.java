package archives.tater.lockedloaded.mixin.enchantmenteffect.ignoreowner;

import archives.tater.lockedloaded.registry.LockedLoadedAttachments;
import archives.tater.lockedloaded.registry.LockedLoadedEnchantmentEffects;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

@SuppressWarnings("UnstableApiUsage")
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {

    public AbstractArrowMixin(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL")
    )
    private void setIgnoreOwner(EntityType<? extends AbstractArrow> type, double x, double y, double z, Level level2, ItemStack pickupItemStack, ItemStack firedFromWeapon, CallbackInfo ci) {
        if (firedFromWeapon == null || !(level2 instanceof ServerLevel serverLevel)) return;
        EnchantmentHelper.runIterationOnItem(firedFromWeapon, (enchantment, _) -> {
            if (!enchantment.value().getEffects(LockedLoadedEnchantmentEffects.PROJECTILE_IGNORE_OWNER).isEmpty())
                setAttached(LockedLoadedAttachments.IGNORE_OWNER, Unit.INSTANCE);
        });
    }

    @ModifyReturnValue(
            method = "canHitEntity",
            at = @At("RETURN")
    )
    private boolean ignoreOwner(boolean original, @Local(argsOnly = true) Entity entity) {
        return original && (!hasAttached(LockedLoadedAttachments.IGNORE_OWNER) || !entity.equals(getOwner()));
    }
}

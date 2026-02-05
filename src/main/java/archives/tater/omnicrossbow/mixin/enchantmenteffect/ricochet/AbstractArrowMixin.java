package archives.tater.omnicrossbow.mixin.enchantmenteffect.ricochet;

import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;
import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantmentEffects;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import org.apache.commons.lang3.mutable.MutableFloat;

import static java.lang.Math.max;

@SuppressWarnings("UnstableApiUsage")
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {
    public AbstractArrowMixin(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getPiercingCount(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)I")
    )
    private void setRicochetCount(EntityType<? extends AbstractArrow> type, double x, double y, double z, Level serverLevel, ItemStack pickupItemStack, ItemStack firedFromWeapon, CallbackInfo ci) {
        var value = new MutableFloat(0);
        EnchantmentHelper.runIterationOnItem(firedFromWeapon, (enchantment, level) ->
                enchantment.value().modifyItemFilteredCount(OmniCrossbowEnchantmentEffects.PROJECTILE_RICOCHET, (ServerLevel) serverLevel, level, firedFromWeapon, value)
        );
        var ricochetCount = max(0, value.intValue());
        if (ricochetCount > 0)
            setAttached(OmniCrossbowAttachments.RICOCHET_LEVEL, ricochetCount);
    }

    @Inject(
            method = "onHitBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;"),
            cancellable = true
    )
    private void ricochet(BlockHitResult hitResult, CallbackInfo ci) {
        int ricochetLevel = getAttachedOrElse(OmniCrossbowAttachments.RICOCHET_LEVEL, 0);
        if (ricochetLevel <= 0) return;

        var movement = getDeltaMovement();
        var axis = hitResult.getDirection().getAxis();
        setPos(hitResult.getLocation().add(hitResult.getDirection().getUnitVec3().scale(getBbWidth())));
        setDeltaMovement(movement.with(axis, -movement.get(axis)));
        needsSync = true;
        setAttached(OmniCrossbowAttachments.RICOCHET_LEVEL, ricochetLevel - 1);

        ci.cancel();
    }
}

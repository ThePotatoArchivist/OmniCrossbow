package archives.tater.lockedloaded.mixin.enchantmenteffect.ricochet;

import archives.tater.lockedloaded.registry.LockedLoadedAttachments;
import archives.tater.lockedloaded.registry.LockedLoadedEnchantmentEffects;
import archives.tater.lockedloaded.registry.LockedLoadedSounds;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import org.apache.commons.lang3.mutable.MutableFloat;
import org.jspecify.annotations.Nullable;

import static java.lang.Math.max;

@SuppressWarnings("UnstableApiUsage")
@Mixin(AbstractArrow.class)
public abstract class AbstractArrowMixin extends Projectile {
    @Shadow
    protected abstract SoundEvent getHitGroundSoundEvent();

    @Shadow
    private @Nullable IntOpenHashSet piercingIgnoreEntityIds;

    @Shadow
    public abstract byte getPierceLevel();

    @Shadow
    protected abstract void setPierceLevel(byte pieceLevel);

    public AbstractArrowMixin(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Inject(
            method = "<init>(Lnet/minecraft/world/entity/EntityType;DDDLnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At("TAIL")
    )
    private void setRicochetCount(EntityType<? extends AbstractArrow> type, double x, double y, double z, Level level2, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon, CallbackInfo ci) {
        if (firedFromWeapon == null || !(level2 instanceof ServerLevel serverLevel)) return;
        setAttached(LockedLoadedAttachments.ORIGINAL_PIERCE_COUNT, getPierceLevel());
        var value = new MutableFloat(0);
        EnchantmentHelper.runIterationOnItem(firedFromWeapon, (enchantment, level) ->
                enchantment.value().modifyItemFilteredCount(LockedLoadedEnchantmentEffects.PROJECTILE_RICOCHET, serverLevel, level, firedFromWeapon, value)
        );
        var ricochetCount = (byte) max(0, value.intValue());
        if (ricochetCount > 0)
            setAttached(LockedLoadedAttachments.RICOCHET_LEVEL, ricochetCount);
    }

    @Inject(
            method = "onHitBlock",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/arrow/AbstractArrow;getDeltaMovement()Lnet/minecraft/world/phys/Vec3;"),
            cancellable = true
    )
    private void ricochet(BlockHitResult hitResult, CallbackInfo ci) {
        byte ricochetLevel = getAttachedOrElse(LockedLoadedAttachments.RICOCHET_LEVEL, (byte) 0);
        if (ricochetLevel <= 0) return;

        var movement = getDeltaMovement();
        var axis = hitResult.getDirection().getAxis();
        setPos(hitResult.getLocation().add(hitResult.getDirection().getUnitVec3().scale(getBbWidth())));
        setDeltaMovement(movement.with(axis, -movement.get(axis)));
        needsSync = true;
        setAttached(LockedLoadedAttachments.RICOCHET_LEVEL, (byte) (ricochetLevel - 1));
        playSound(LockedLoadedSounds.RICOCHET, 1.0F, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
        if (piercingIgnoreEntityIds != null)
            piercingIgnoreEntityIds.clear();
        byte originalPierceCount = getAttachedOrElse(LockedLoadedAttachments.ORIGINAL_PIERCE_COUNT, (byte) 0);
        if (originalPierceCount > getPierceLevel())
            setPierceLevel(originalPierceCount);

        ci.cancel();
    }
}

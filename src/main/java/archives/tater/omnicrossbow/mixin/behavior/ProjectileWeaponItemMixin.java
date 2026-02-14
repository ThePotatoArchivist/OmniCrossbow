package archives.tater.omnicrossbow.mixin.behavior;

import archives.tater.omnicrossbow.entity.DelegateProjectile;
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior;
import archives.tater.omnicrossbow.projectilebehavior.DelayTracker;
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.Delegated;
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.SpawnProjectile;
import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.component.ChargedProjectiles;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static archives.tater.omnicrossbow.util.OmniUtil.getOrSet;
import static java.lang.Math.max;
import static java.util.Objects.requireNonNullElse;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @SuppressWarnings({"UnstableApiUsage"})
    @WrapOperation(
            method = "shoot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/entity/projectile/Projectile;")
    )
    private Projectile modifyProjectileShoot(Projectile projectile, ServerLevel serverLevel, ItemStack itemStack, Consumer<Projectile> shootFunction, Operation<Projectile> original, @Local(argsOnly = true, ordinal = 0) LivingEntity shooter, @Local(argsOnly = true) ItemStack weapon, @Share("cooldown") LocalIntRef cooldown, @Share("delayedProjectiles") LocalRef<@Nullable List<ItemStack>> delayedProjectiles) {
        var behavior = ProjectileBehavior.getBehavior(serverLevel, itemStack);

        Supplier<Projectile> shoot = () -> {

            var usedProjectile = switch (behavior.projectileAction()) {
                case SpawnProjectile<?> spawnProjectile -> requireNonNullElse(spawnProjectile.createProjectile(serverLevel, shooter, weapon, itemStack), projectile);
                case Delegated _ -> new DelegateProjectile(serverLevel, shooter);
                default -> projectile;
            };

            usedProjectile.setAttached(OmniCrossbowAttachments.PROJECTILE_BEHAVIOR, behavior);

            if (!itemStack.has(DataComponents.INTANGIBLE_PROJECTILE)) {
                var remainder = behavior.getRemainder(itemStack);
                if (remainder != null) shooter.handleExtraItemsCreatedOnUse(remainder);
            }

            cooldown.set(max(cooldown.get(), behavior.cooldownTicks()));

            if (!(behavior.projectileAction() instanceof Delegated delegated)) return original.call(usedProjectile, serverLevel, itemStack, shootFunction);

            shootFunction.accept(usedProjectile);
            delegated.shoot(usedProjectile.position(), usedProjectile.getDeltaMovement(), serverLevel, shooter, weapon, itemStack);

            return usedProjectile;
        };

        if (behavior.delay().isPresent()) {
            var delay = behavior.delay().get();
            var delayTicks = delay.ticks().sample(shooter.getRandom()) - shooter.getTicksUsingItem(); // spinning
            if (delayTicks > 0) {
                if (!shooter.isUsingItem()) delay.chargeSound().ifPresent(sound ->
                        serverLevel.playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), sound, shooter.getSoundSource(), 1f, 1f));

                shooter.getAttachedOrCreate(OmniCrossbowAttachments.DELAYED_SHOTS, DelayTracker::new)
                        .getEntries()
                        .add(new DelayTracker.Entry(
                                delayTicks,
                                shoot::get,
                                weapon,
                                itemStack
                        ));

                getOrSet(delayedProjectiles, ArrayList::new).add(itemStack);

                return projectile;
            }
        }

        return shoot.get();
    }

    @Inject(
            method = "shoot",
            at = @At("TAIL")
    )
    private void setCooldownAndReinsertDelayed(ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, List<ItemStack> projectiles, float power, float uncertainty, boolean isCrit, @Nullable LivingEntity targetOverride, CallbackInfo ci, @Share("cooldown") LocalIntRef cooldown, @Share("delayedProjectiles") LocalRef<@Nullable List<ItemStack>> delayedProjectiles) {
        if (cooldown.get() > 0 && shooter.onGround() && shooter instanceof Player player && !player.hasInfiniteMaterials())
            player.getCooldowns().addCooldown(weapon, cooldown.get());

        if (weapon.has(DataComponents.CHARGED_PROJECTILES)) {
            var delayed = delayedProjectiles.get();
            if (delayed != null)
                weapon.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.ofNonEmpty(Stream.concat(
                        weapon.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).itemCopies().stream(),
                        delayed.stream()
                ).toList()));
        }
    }
}

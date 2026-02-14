package archives.tater.omnicrossbow.mixin.behavior;

import archives.tater.omnicrossbow.entity.DelegateProjectile;
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior;
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.DelayedShot;
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.Delegated;
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.ProjectileAction;
import archives.tater.omnicrossbow.projectilebehavior.projectileaction.SpawnProjectile;
import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
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

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static java.lang.Math.max;
import static java.util.Objects.requireNonNullElse;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {

    @SuppressWarnings({"UnstableApiUsage"})
    @WrapOperation(
            method = "shoot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/entity/projectile/Projectile;")
    )
    private Projectile modifyProjectileShoot(Projectile projectile, ServerLevel serverLevel, ItemStack itemStack, Consumer<Projectile> shootFunction, Operation<Projectile> original, @Local(argsOnly = true, ordinal = 0) LivingEntity shooter, @Local(argsOnly = true) ItemStack weapon, @Share("cooldown") LocalIntRef cooldown) {
        var behavior = ProjectileBehavior.getBehavior(serverLevel, itemStack);

        var usedProjectile = switch (behavior.projectileAction()) {
            case SpawnProjectile<?> spawnProjectile -> requireNonNullElse(spawnProjectile.createProjectile(serverLevel, shooter, weapon, itemStack), projectile);
            case Delegated _, DelayedShot _ -> new DelegateProjectile(serverLevel, shooter);
            default -> projectile;
        };

        usedProjectile.setAttached(OmniCrossbowAttachments.PROJECTILE_BEHAVIOR, behavior);

        if (!itemStack.has(DataComponents.INTANGIBLE_PROJECTILE)) {
            var remainder = behavior.getRemainder(itemStack);
            if (remainder != null) shooter.handleExtraItemsCreatedOnUse(remainder);
        }

        cooldown.set(max(cooldown.get(), behavior.cooldownTicks()));

        Function<ProjectileAction, Projectile> shoot = action -> switch (action) {
            case Delegated delegated -> {
                shootFunction.accept(usedProjectile);
                delegated.shoot(usedProjectile.position(), usedProjectile.getDeltaMovement(), serverLevel, shooter, weapon, itemStack);

                yield usedProjectile;
            }
            default -> original.call(usedProjectile, serverLevel, itemStack, shootFunction);
        };

        if (behavior.projectileAction() instanceof DelayedShot delayedShot) {
            var delay = delayedShot.delay().sample(shooter.getRandom()) - shooter.getTicksUsingItem(); // spinning
            if (delay <= 0) {
                return shoot.apply(behavior.projectileAction());
            }
            shooter.getAttachedOrCreate(OmniCrossbowAttachments.DELAYED_SHOTS, DelayedShot.Tracker::new)
                    .getEntries()
                    .add(new DelayedShot.Tracker.Entry(
                            delay,
                            () -> shoot.apply(delayedShot.action()),
                            weapon,
                            itemStack
                    ));
            return usedProjectile;
        }

        return shoot.apply(behavior.projectileAction());
    }

    @Inject(
            method = "shoot",
            at = @At("TAIL")
    )
    private void setCooldown(ServerLevel level, LivingEntity shooter, InteractionHand hand, ItemStack weapon, List<ItemStack> projectiles, float power, float uncertainty, boolean isCrit, @Nullable LivingEntity targetOverride, CallbackInfo ci, @Share("cooldown") LocalIntRef cooldown) {
        if (cooldown.get() > 0 && shooter.onGround() && shooter instanceof Player player && !player.hasInfiniteMaterials())
            player.getCooldowns().addCooldown(weapon, cooldown.get());
    }
}

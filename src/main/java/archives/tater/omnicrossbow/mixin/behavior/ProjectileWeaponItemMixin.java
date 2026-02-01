package archives.tater.omnicrossbow.mixin.behavior;

import archives.tater.omnicrossbow.entity.DelegateProjectile;
import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior;
import archives.tater.omnicrossbow.projectilebehavior.action.Delegated;
import archives.tater.omnicrossbow.projectilebehavior.action.SpawnProjectile;
import archives.tater.omnicrossbow.registry.OmniCrossbowAttachments;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.Nullable;

import java.util.function.Consumer;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @WrapOperation(
            method = "shoot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;createProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/projectile/Projectile;")
    )
    private Projectile modifyProjectile(ProjectileWeaponItem instance, Level level, LivingEntity shooter, ItemStack weapon, ItemStack projectile, boolean isCrit, Operation<Projectile> original, @Share("projectileBehavior") LocalRef<@Nullable ProjectileBehavior> projectileBehavior) {
        var behavior = ProjectileBehavior.getBehavior(level, projectile);
        if (behavior == null) return original.call(instance, level, shooter, weapon, projectile, isCrit);

        projectileBehavior.set(behavior);

        return switch (behavior.projectileAction()) {
            case SpawnProjectile spawnProjectile -> spawnProjectile.createProjectile(level, shooter, weapon, projectile);
            case Delegated ignored -> new DelegateProjectile(level, shooter);
            default -> original.call(instance, level, shooter, weapon, projectile, isCrit);
        };
    }

    @SuppressWarnings("UnstableApiUsage")
    @WrapOperation(
            method = "shoot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/entity/projectile/Projectile;")
    )
    private <T extends Projectile> T modifyProjectileShoot(T projectile, ServerLevel serverLevel, ItemStack itemStack, Consumer<T> shootFunction, Operation<T> original, @Local(argsOnly = true, ordinal = 0) LivingEntity shooter, @Local(argsOnly = true) ItemStack weapon, @Local(name = "projectile") ItemStack projectileItem, @Share("projectileBehavior") LocalRef<@Nullable ProjectileBehavior> projectileBehavior) {
        var behavior = projectileBehavior.get();
        if (behavior == null) return original.call(projectile, serverLevel, itemStack, shootFunction);

        projectile.setAttached(OmniCrossbowAttachments.VELOCITY_SCALE, behavior.velocityScale());
        behavior.shootSound().ifPresent(sound ->
                projectile.setAttached(OmniCrossbowAttachments.SHOOT_SOUND, sound)
        );
        if (!projectileItem.has(DataComponents.INTANGIBLE_PROJECTILE)) {
            var remainder = behavior.getRemainder(projectileItem);
            if (remainder != null) shooter.handleExtraItemsCreatedOnUse(remainder);
        }

        if (!(behavior.projectileAction() instanceof Delegated delegated)) return original.call(projectile, serverLevel, itemStack, shootFunction);

        shootFunction.accept(projectile);
        delegated.shoot(projectile.position(), projectile.getDeltaMovement(), serverLevel, shooter, weapon, projectileItem);

        return projectile;
    }
}

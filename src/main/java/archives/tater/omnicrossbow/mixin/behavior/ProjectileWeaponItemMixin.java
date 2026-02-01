package archives.tater.omnicrossbow.mixin.behavior;

import archives.tater.omnicrossbow.projectilebehavior.ProjectileBehavior;
import archives.tater.omnicrossbow.projectilebehavior.action.Delegated;
import archives.tater.omnicrossbow.projectilebehavior.action.ProjectileAction;
import archives.tater.omnicrossbow.projectilebehavior.action.SpawnProjectile;
import archives.tater.omnicrossbow.registry.OmniCrossbowRegistries;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.Nullable;

@Mixin(ProjectileWeaponItem.class)
public class ProjectileWeaponItemMixin {
    @WrapOperation(
            method = "shoot",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;createProjectile(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/world/entity/projectile/Projectile;")
    )
    private Projectile modifyProjectile(ProjectileWeaponItem instance, Level level, LivingEntity shooter, ItemStack weapon, ItemStack projectile, boolean isCrit, Operation<Projectile> original, @Share("projectileBehavior") LocalRef<@Nullable ProjectileBehavior> projectileBehavior) {
        var behavior = level.registryAccess().lookupOrThrow(OmniCrossbowRegistries.PROJECTILE_BEHAVIOR).stream()
                .filter(b -> projectile.is(b.items()))
                .findFirst()
                .orElse(null); // TODO fallback
        if (behavior == null) return original.call(instance, level, shooter, weapon, projectile, isCrit);
        projectileBehavior.set(behavior);
        return switch (behavior.projectileAction()) {
            case SpawnProjectile spawnProjectile -> spawnProjectile.createProjectile(level, shooter, weapon, projectile, isCrit);
            case Delegated ignored -> null;
            case ProjectileAction.Default ignored -> original.call(instance, level, shooter, weapon, projectile, isCrit);
            default -> throw new AssertionError("Invalid subclass");
        };
    }

//    @WrapOperation(
//            method = "shoot",
//            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/Projectile;spawnProjectile(Lnet/minecraft/world/entity/projectile/Projectile;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;)Lnet/minecraft/world/entity/projectile/Projectile;")
//    )
//    private <T extends Projectile> T modifyProjectileShoot(T projectile, ServerLevel serverLevel, ItemStack itemStack, Consumer<T> shootFunction, Operation<T> original) {
//        return null;
//    }
}

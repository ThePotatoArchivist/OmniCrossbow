package archives.tater.omnicrossbow.mixin.enchantmenteffect.anyprojectile;

import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantmentEffects;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.function.Predicate;

@Mixin({Player.class, Monster.class})
public abstract class ProjectileUserMixin extends LivingEntity {
    protected ProjectileUserMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @ModifyExpressionValue(
            method = "getProjectile",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ProjectileWeaponItem;getSupportedHeldProjectiles()Ljava/util/function/Predicate;")
    )
    private Predicate<ItemStack> anyProjectileEnchantment(Predicate<ItemStack> original, ItemStack heldWeapon) {
        return EnchantmentHelper.has(heldWeapon, OmniCrossbowEnchantmentEffects.ALLOW_ANY_PROJECTILE)
                ? projectile -> projectile != heldWeapon
                : original;
    }
}

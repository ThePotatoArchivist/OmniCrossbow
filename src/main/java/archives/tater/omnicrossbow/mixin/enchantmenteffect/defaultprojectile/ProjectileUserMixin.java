package archives.tater.omnicrossbow.mixin.enchantmenteffect.defaultprojectile;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import static archives.tater.omnicrossbow.util.OmniEnchantmentHelperKt.getDefaultProjectile;

@Mixin({Player.class, Monster.class})
public abstract class ProjectileUserMixin extends LivingEntity {
    protected ProjectileUserMixin(EntityType<? extends LivingEntity> type, Level level) {
        super(type, level);
    }

    @WrapOperation(
            method = "getProjectile",
            at = @At(value = "NEW", target = "(Lnet/minecraft/world/level/ItemLike;)Lnet/minecraft/world/item/ItemStack;")
    )
    private ItemStack defaultProjectile(ItemLike item, Operation<ItemStack> original, ItemStack heldWeapon) {
        if (!(level() instanceof ServerLevel level)) return original.call(item);
        var result = getDefaultProjectile(heldWeapon, level, this);
        return result == null ? original.call(item) : result;
    }

}

package archives.tater.omnicrossbow.mixin;

import archives.tater.omnicrossbow.OmniCrossbow;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(HostileEntity.class)
public abstract class HostileEntityMixin extends PathAwareEntity {
    protected HostileEntityMixin(EntityType<? extends PathAwareEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyExpressionValue(
            method = "getProjectileType",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/item/RangedWeaponItem;getHeldProjectiles()Ljava/util/function/Predicate;")
    )
    private Predicate<ItemStack> allowAnything(Predicate<ItemStack> original, @Local(argsOnly = true) ItemStack itemStack) {
        if (itemStack.getItem() instanceof CrossbowItem && EnchantmentHelper.getLevel(OmniCrossbow.OMNI, itemStack) > 0) {
            return stack -> !stack.isEmpty() && stack != itemStack;
        }
        return original;
    }

//    @ModifyExpressionValue(
//            method = "getProjectileType",
//            at = @At(value = "NEW", target = "(Lnet/minecraft/item/ItemConvertible;)Lnet/minecraft/item/ItemStack;")
//    )
//    private ItemStack randomAmmo(ItemStack original, @Local(argsOnly = true) ItemStack crossbow) {
//        if (crossbow.isOf(Items.CROSSBOW) && EnchantmentHelper.getLevel(OmniCrossbow.OMNI, crossbow) > 0)
//            return OmniEnchantment.getRandomAmmo(random).getDefaultStack();
//        return original;
//    }
}

package archives.tater.omnicrossbow;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class MultichamberedEnchantment {
    // Static utility class
    private MultichamberedEnchantment() {}

    public static boolean hasMultichambered(ItemStack crossbow) {
        return EnchantmentHelper.hasAnyEnchantmentsWith(crossbow, OmniCrossbowEnchantmentEffects.ONE_PROJECTILE_AT_TIME);
    }

    public static int getLoadedShots(ItemStack crossbow) {
        return crossbow.getOrDefault(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT).getProjectiles().size();
    }

    public static boolean cannotLoadMore(ItemStack stack) {
        return hasMultichambered(stack) && stack.contains(OmniCrossbow.CROSSBOW_FULL);
    }

    public static ItemStack getPrimaryCrossbow(LivingEntity livingEntity) {
        for (var hand : Hand.values()) {
            var stack = livingEntity.getStackInHand(hand);
            if (stack.isOf(Items.CROSSBOW) && hasMultichambered(stack))
                return stack;
        }
        return ItemStack.EMPTY;
    }

    public static void unloadOneProjectile(ItemStack crossbow) {
        var projectiles = crossbow.getOrDefault(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT).getProjectiles();
        if (projectiles.size() <= 1) {
            crossbow.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.DEFAULT);
            return;
        }
        crossbow.set(DataComponentTypes.CHARGED_PROJECTILES, ChargedProjectilesComponent.of(projectiles.subList(1, projectiles.size())));
    }

}

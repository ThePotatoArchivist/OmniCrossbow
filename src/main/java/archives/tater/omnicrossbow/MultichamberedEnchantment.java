package archives.tater.omnicrossbow;

import net.minecraft.enchantment.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtElement;
import net.minecraft.util.Hand;

public class MultichamberedEnchantment extends Enchantment {
    private static final int POWER_PER_LEVEL = 7;

    protected MultichamberedEnchantment(Rarity weight, EquipmentSlot... slotTypes) {
        super(weight, EnchantmentTarget.CROSSBOW, slotTypes);
    }

    @Override
    protected boolean canAccept(Enchantment other) {
        return super.canAccept(other) && !(other instanceof MultishotEnchantment) && !(other instanceof PiercingEnchantment);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public int getMinPower(int level) {
        return 10 + (level - 1) * POWER_PER_LEVEL;
    }

    @Override
    public int getMaxPower(int level) {
        return getMinPower(level) + POWER_PER_LEVEL;
    }

    public static int getMaxShots(int level) {
        return 2 * level;
    }

    public static int getMaxShots(ItemStack crossbow) {
        return getMaxShots(EnchantmentHelper.getLevel(OmniCrossbow.MULTICHAMBERED, crossbow));
    }

    public static boolean hasMultichambered(ItemStack crossbow) {
        return EnchantmentHelper.getLevel(OmniCrossbow.MULTICHAMBERED, crossbow) > 0;
    }

    public static int getLoadedShots(ItemStack crossbow) {
        var nbt = crossbow.getNbt();
        if (nbt == null) return 0;
        return nbt.getList("ChargedProjectiles", NbtElement.COMPOUND_TYPE).size();
    }

    public static boolean cannotLoadMore(ItemStack stack) {
        return !hasMultichambered(stack) || getLoadedShots(stack) >= getMaxShots(stack);
    }

    public static ItemStack getPrimaryCrossbow(LivingEntity livingEntity) {
        for (var hand : Hand.values()) {
            var stack = livingEntity.getStackInHand(hand);
            if (stack.isOf(Items.CROSSBOW) && hasMultichambered(stack))
                return stack;
        }
        return ItemStack.EMPTY;
    }

}

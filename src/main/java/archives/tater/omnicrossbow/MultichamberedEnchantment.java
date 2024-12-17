package archives.tater.omnicrossbow;

import net.minecraft.enchantment.*;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

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

    public static boolean hasMultichambered(ItemStack crossbow) {
        return EnchantmentHelper.getLevel(OmniCrossbow.MULTICHAMBERED, crossbow) > 0;
    }
}

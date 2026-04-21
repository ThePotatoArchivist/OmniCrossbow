package archives.tater.omnicrossbow.mixin.enchantmentprovider;

import archives.tater.omnicrossbow.registry.OmniCrossbowEnchantments;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.illager.Pillager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

@Mixin(Pillager.class)
public abstract class PillagerMixin extends AbstractIllager {
    protected PillagerMixin(EntityType<? extends AbstractIllager> type, Level level) {
        super(type, level);
    }

    @WrapMethod(
            method = "applyRaidBuffs"
    )
    private void finalWaveEnchantment(ServerLevel level, int wave, boolean isCaptain, Operation<Void> original) {
        var raid = getCurrentRaid();
        if (raid == null || wave < ((RaidAccessor) raid).getNumGroups() || raid.getAllRaiders().stream().filter(Pillager.class::isInstance).limit(2).count() > 1) {
            original.call(level, wave, isCaptain);
            return;
        }
        var crossbow = new ItemStack(Items.CROSSBOW);
        EnchantmentHelper.enchantItemFromProvider(crossbow, level.registryAccess(), OmniCrossbowEnchantments.RAID_PILLAGER_FINAL_WAVE_UNIQUE, level.getCurrentDifficultyAt(this.blockPosition()), this.getRandom());
        setItemSlot(EquipmentSlot.MAINHAND, crossbow);
    }
}

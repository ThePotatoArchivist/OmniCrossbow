package archives.tater.omnicrossbow.mixin.behavior;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

@Mixin(LivingEntity.class)
public interface LivingEntityInvoker {
    @Invoker
    Map<EquipmentSlot, ItemStack> invokeCollectEquipmentChanges();

    @Accessor
    void setAttackStrengthTicker(int attackStrengthTicker);
}

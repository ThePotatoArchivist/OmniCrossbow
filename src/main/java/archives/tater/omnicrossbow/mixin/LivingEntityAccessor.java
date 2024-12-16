package archives.tater.omnicrossbow.mixin;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor
    void setLastAttackedTicks(int ticks);

    @Invoker
    @Nullable Map<EquipmentSlot, ItemStack> invokeGetEquipmentChanges();
}

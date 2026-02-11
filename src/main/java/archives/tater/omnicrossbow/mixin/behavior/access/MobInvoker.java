package archives.tater.omnicrossbow.mixin.behavior.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

@Mixin(Mob.class)
public interface MobInvoker {
    @Invoker
    void invokeSetItemSlotAndDropWhenKilled(final EquipmentSlot slot, final ItemStack itemStack);
}

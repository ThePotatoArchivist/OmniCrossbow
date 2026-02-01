package archives.tater.omnicrossbow.mixin.behavior;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.item.ItemEntity;

@Mixin(ItemEntity.class)
public interface ItemEntityAccessor {
    @Accessor
    void setAge(int age);
}

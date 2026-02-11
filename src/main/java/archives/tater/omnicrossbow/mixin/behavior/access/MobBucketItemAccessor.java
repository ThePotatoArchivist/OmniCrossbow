package archives.tater.omnicrossbow.mixin.behavior.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.MobBucketItem;

@Mixin(MobBucketItem.class)
public interface MobBucketItemAccessor {
    @Accessor
    EntityType<? extends Mob> getType();
}

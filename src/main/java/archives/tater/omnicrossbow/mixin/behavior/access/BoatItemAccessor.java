package archives.tater.omnicrossbow.mixin.behavior.access;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.BoatItem;

@Mixin(BoatItem.class)
public interface BoatItemAccessor {
    @Accessor
    EntityType<? extends AbstractBoat> getEntityType();
}

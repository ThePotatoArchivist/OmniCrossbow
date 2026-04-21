package archives.tater.omnicrossbow.mixin.enchantmentprovider;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.raid.Raid;

@Mixin(Raid.class)
public interface RaidAccessor {
    @Accessor
    int getNumGroups();
}

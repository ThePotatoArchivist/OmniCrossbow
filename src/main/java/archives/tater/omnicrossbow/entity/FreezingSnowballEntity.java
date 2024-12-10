package archives.tater.omnicrossbow.entity;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class FreezingSnowballEntity extends SnowballEntity {
    public FreezingSnowballEntity(EntityType<? extends FreezingSnowballEntity> entityType, World world) {
        super(entityType, world);
    }

    public FreezingSnowballEntity(World world, double x, double y, double z) {
        this(OmniCrossbowEntities.FREEZING_SNOWBALL, world);
        setPosition(x, y, z);
    }

    public FreezingSnowballEntity(World world, LivingEntity owner) {
        this(world, owner.getX(), owner.getEyeY() - 0.1f, owner.getZ());
        setOwner(owner);
    }

    @Override
    protected void onEntityHit(EntityHitResult entityHitResult) {
        super.onEntityHit(entityHitResult);
        var entity = entityHitResult.getEntity();
        entity.setFrozenTicks(entity.getFrozenTicks() + entity.getMinFreezeDamageTicks());
    }
}

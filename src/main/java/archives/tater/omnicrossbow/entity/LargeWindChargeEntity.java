package archives.tater.omnicrossbow.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractWindChargeEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.AdvancedExplosionBehavior;
import net.minecraft.world.explosion.ExplosionBehavior;

import java.util.Optional;
import java.util.function.Function;

public class LargeWindChargeEntity extends AbstractWindChargeEntity {
    private static final ExplosionBehavior EXPLOSION_BEHAVIOR = new AdvancedExplosionBehavior(
            true, false, Optional.of(1.3f), Registries.BLOCK.getEntryList(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
    );
    private static final float EXPLOSION_POWER = 3.0F;

    public LargeWindChargeEntity(EntityType<? extends AbstractWindChargeEntity> entityType, World world) {
        super(entityType, world);
    }

    public LargeWindChargeEntity(Entity owner, World world, double x, double y, double z) {
        super(OmniCrossbowEntities.LARGE_WIND_CHARGE, world, owner, x, y, z);
    }

    public LargeWindChargeEntity(World world, Entity owner) {
        super(OmniCrossbowEntities.LARGE_WIND_CHARGE, world, owner, owner.getX(), owner.getEyeY() - 0.1f, owner.getZ());
    }

    public LargeWindChargeEntity(World world, double x, double y, double z, Vec3d velocity) {
        super(OmniCrossbowEntities.LARGE_WIND_CHARGE, x, y, z, velocity, world);
    }

    @Override
    protected void createExplosion(Vec3d pos) {
        this.getWorld()
                .createExplosion(
                        this,
                        null,
                        EXPLOSION_BEHAVIOR,
                        pos.getX(),
                        pos.getY(),
                        pos.getZ(),
                        EXPLOSION_POWER,
                        false,
                        World.ExplosionSourceType.TRIGGER,
                        ParticleTypes.GUST_EMITTER_SMALL,
                        ParticleTypes.GUST_EMITTER_LARGE,
                        SoundEvents.ENTITY_WIND_CHARGE_WIND_BURST
                );
    }

}

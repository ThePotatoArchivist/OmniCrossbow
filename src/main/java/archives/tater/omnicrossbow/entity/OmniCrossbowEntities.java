package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.OmniCrossbow;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class OmniCrossbowEntities {
    private static <T extends Entity> EntityType<T> register(Identifier id, EntityType<T> entityType) {
        return Registry.register(Registries.ENTITY_TYPE, id, entityType);
    }

    private static <T extends Entity> EntityType<T> register(String path, EntityType<T> entityType) {
        return register(OmniCrossbow.id(path), entityType);
    }

    private static <T extends Entity> EntityType<T> register(String path, FabricEntityTypeBuilder<T> entityType) {
        return register(OmniCrossbow.id(path), entityType.build());
    }

    private static <T extends Entity> FabricEntityTypeBuilder<T> misc(EntityType.EntityFactory<T> factory, float width, float height, int trackRangeChunks, int trackedUpdateRate) {
        return FabricEntityTypeBuilder.create(SpawnGroup.MISC, factory)
                .dimensions(EntityDimensions.fixed(width, height))
                .trackRangeChunks(trackRangeChunks)
                .trackedUpdateRate(trackedUpdateRate);
    }

    private static <T extends Entity> FabricEntityTypeBuilder<T> defaultProjectile(EntityType.EntityFactory<T> factory) {
        return misc(factory, 0.25f, 0.25f, 4, 10);
    }

    public static EntityType<FreezingSnowballEntity> FREEZING_SNOWBALL = register(
            "freezing_snowball",
            defaultProjectile(FreezingSnowballEntity::new)
    );

    public static EntityType<DelayedSonicBoomEntity> DELAYED_SONIC_BOOM = register(
            "sonic_boom",
            misc(DelayedSonicBoomEntity::new, 0, 0, 0, 10)
    );

    public static EntityType<BeaconLaserEntity> BEACON_LASER = register(
            "beacon_laser",
            misc(BeaconLaserEntity::new, 1f, 1f, 8, 3)
    );

    public static EntityType<GenericItemProjectile> GENERIC_ITEM_PROJECTILE = register(
            "item_projectile",
            defaultProjectile(GenericItemProjectile::new)
    );

    public static EntityType<EmberEntity> EMBER = register(
            "ember",
            defaultProjectile(EmberEntity::new)
    );

    public static EntityType<SlimeballEntity> SLIME_BALL = register(
            "slime_ball",
            defaultProjectile(SlimeballEntity::new)
    );

    public static EntityType<EndCrystalProjectileEntity> END_CRYSTAL_PROJECTILE = register(
            "end_crystal_projecctile",
            OmniCrossbowEntities.<EndCrystalProjectileEntity>misc(EndCrystalProjectileEntity::new, 1.2f, 1.2f, 6, 10)
                    .fireImmune()
    );

    public static EntityType<SpyEnderEyeEntity> SPY_ENDER_EYE = register(
            "spy_ender_eye",
            misc(SpyEnderEyeEntity::new, 0.25f, 0.25f, 16, 10)
    );

    public static void register() {}
}

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
        return register(new Identifier(OmniCrossbow.MOD_ID, path), entityType);
    }

    private static <T extends Entity> EntityType<T> register(String path, FabricEntityTypeBuilder<T> entityType) {
        return register(new Identifier(OmniCrossbow.MOD_ID, path), entityType.build());
    }

    public static EntityType<FreezingSnowballEntity> FREEZING_SNOWBALL = register(
            "freezing_snowball",
            FabricEntityTypeBuilder.<FreezingSnowballEntity>create(SpawnGroup.MISC, FreezingSnowballEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeChunks(4)
                    .trackedUpdateRate(10)
    );

    public static EntityType<DelayedSonicBoomEntity> DELAYED_SONIC_BOOM = register(
            "sonic_boom",
            FabricEntityTypeBuilder.<DelayedSonicBoomEntity>create(SpawnGroup.MISC, DelayedSonicBoomEntity::new)
                    .dimensions(EntityDimensions.fixed(0, 0))
                    .trackRangeChunks(0)
    );

    public static EntityType<GenericItemProjectile> GENERIC_ITEM_PROJECTILE = register(
            "item_projectile",
            FabricEntityTypeBuilder.<GenericItemProjectile>create(SpawnGroup.MISC, GenericItemProjectile::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackedUpdateRate(4)
                    .trackedUpdateRate(10)
    );

    public static void register() {};
}

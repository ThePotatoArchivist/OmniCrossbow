package archives.tater.omnicrossbow.entity;

import archives.tater.omnicrossbow.OmniCrossbow;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class OmniCrossbowEntities {
    public static EntityType<CrossbowSnowballEntity> CROSSBOW_SNOWBALL = Registry.register(
            Registries.ENTITY_TYPE,
            new Identifier(OmniCrossbow.MOD_ID, "snowball"),
            FabricEntityTypeBuilder.<CrossbowSnowballEntity>create(SpawnGroup.MISC, CrossbowSnowballEntity::new)
                    .dimensions(EntityDimensions.fixed(0.25f, 0.25f))
                    .trackRangeChunks(4)
                    .trackedUpdateRate(10)
                    .build()
    );

    public static void register() {};
}

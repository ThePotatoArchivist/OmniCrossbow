package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricTrackedDataRegistry
import net.minecraft.core.Direction
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityReference
import java.util.*

object OmniCrossbowEntityDataSerializers {
    private fun <T: Any> register(path: String, serializer: EntityDataSerializer<T>) = serializer.also {
        FabricTrackedDataRegistry.register(OmniCrossbow.id(path), it)
    }

    val OPTIONAL_ENTITY_REFERENCE: EntityDataSerializer<Optional<EntityReference<Entity>>> = register(
        "optional_entity_reference",
        EntityDataSerializer.forValueType(EntityReference.streamCodec<Entity>().apply(ByteBufCodecs::optional))
    )

    val OPTIONAL_DIRECTION: EntityDataSerializer<Optional<Direction>> = register(
        "optional_direction",
        EntityDataSerializer.forValueType(Direction.STREAM_CODEC.apply(ByteBufCodecs::optional))
    )

    fun init() {

    }
}
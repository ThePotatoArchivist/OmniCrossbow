package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.condition.BreakingTimeProvider
import archives.tater.omnicrossbow.condition.CanPickUpLoot
import archives.tater.omnicrossbow.condition.ConsumablePredicate
import archives.tater.omnicrossbow.condition.SingletonLootCondition
import archives.tater.omnicrossbow.util.get
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.core.component.DataComponents
import net.minecraft.core.component.predicates.DataComponentPredicate
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.context.ContextKey
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition

object OmniCrossbowConditions {
    private fun <T: LootItemCondition> register(path: String, codec: MapCodec<T>): MapCodec<T> =
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, OmniCrossbow.id(path), codec)

    private fun register(path: String, condition: SingletonLootCondition) = condition.apply {
        register(path, codec)
    }

    private fun register(path: String, vararg keys: ContextKey<*>, test: (LootContext) -> Boolean) =
        register(path, SingletonLootCondition(*keys, test = test))

    val TOOL_SUITABLE_FOR_BLOCK = register("tool_suitable_for_block", LootContextParams.BLOCK_STATE, LootContextParams.TOOL) {
        val state = it.getOptionalParameter(LootContextParams.BLOCK_STATE) ?: return@register false
        it[LootContextParams.TOOL]
            .get(DataComponents.TOOL)
            ?.isCorrectForDrops(state) == true
    }

    val CONSUMABLE_PREDICATE: DataComponentPredicate.ConcreteType<ConsumablePredicate> = Registry.register(
        BuiltInRegistries.DATA_COMPONENT_PREDICATE_TYPE,
        OmniCrossbow.id("consumable"),
        DataComponentPredicate.ConcreteType(ConsumablePredicate.CODEC)
    )

    fun init() {
        Registry.register(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, OmniCrossbow.id("breaking_time"), BreakingTimeProvider.CODEC)
        register("can_pick_up_loot", CanPickUpLoot.CODEC)
    }
}
package archives.tater.lockedloaded.registry

import archives.tater.lockedloaded.LockedLoaded
import archives.tater.lockedloaded.condition.BreakingTimeProvider
import archives.tater.lockedloaded.condition.CanPickUpLoot
import archives.tater.lockedloaded.condition.SingletonLootCondition
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.util.context.ContextKey
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition

object LockedLoadedConditions {
    private fun <T: LootItemCondition> register(path: String, codec: MapCodec<T>): MapCodec<T> =
        Registry.register(BuiltInRegistries.LOOT_CONDITION_TYPE, LockedLoaded.id(path), codec)

    private fun register(path: String, condition: SingletonLootCondition) = condition.apply {
        register(path, codec)
    }

    private fun register(path: String, vararg keys: ContextKey<*>, test: (LootContext) -> Boolean) =
        register(path, SingletonLootCondition(*keys, test = test))



    fun init() {
        Registry.register(BuiltInRegistries.LOOT_NUMBER_PROVIDER_TYPE, LockedLoaded.id("breaking_time"), BreakingTimeProvider.CODEC)
        register("can_pick_up_loot", CanPickUpLoot.CODEC)
    }
}
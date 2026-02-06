package archives.tater.omnicrossbow.condition

import com.mojang.serialization.MapCodec
import net.minecraft.util.context.ContextKey
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition

class SingletonLootCondition(vararg keys: ContextKey<*>, val test: (LootContext) -> Boolean) : LootItemCondition {
    val codec: MapCodec<SingletonLootCondition> = MapCodec.unit(this)
    val keys = setOf(*keys)
    val builder = LootItemCondition.Builder { this@SingletonLootCondition }

    override fun codec(): MapCodec<out LootItemCondition> = codec

    override fun getReferencedContextParams(): Set<ContextKey<*>> = keys

    override fun test(context: LootContext): Boolean = test.invoke(context)
}
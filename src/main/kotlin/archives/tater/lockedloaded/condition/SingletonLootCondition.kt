package archives.tater.lockedloaded.condition

import com.mojang.serialization.MapCodec
import net.minecraft.util.context.ContextKey
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition

class SingletonLootCondition(vararg keys: ContextKey<*>, val test: (LootContext) -> Boolean) : LootItemCondition, LootItemCondition.Builder {
    val codec: MapCodec<SingletonLootCondition> = MapCodec.unit(this)
    val keys = setOf(*keys)

    override fun codec(): MapCodec<out LootItemCondition> = codec

    override fun getReferencedContextParams(): Set<ContextKey<*>> = keys

    override fun test(context: LootContext): Boolean = test.invoke(context)

    override fun build(): LootItemCondition = this
}
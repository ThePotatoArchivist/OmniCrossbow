package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.util.getValue
import net.fabricmc.fabric.api.loot.v3.LootTableEvents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.storage.loot.BuiltInLootTables
import net.minecraft.world.level.storage.loot.LootTable
import net.minecraft.world.level.storage.loot.entries.NestedLootTable.lootTableReference

object OmniCrossbowLoot {
    private fun of(path: String) = ResourceKey.create(Registries.LOOT_TABLE, OmniCrossbow.id(path))
    private fun injectOf(table: ResourceKey<LootTable>) = of("inject/${table.identifier().namespace}/${table.identifier().path}")

    @JvmField
    internal val REGISTRY_OPS = ScopedValue.newInstance<RegistryOps<*>>()
    private val registryOps by REGISTRY_OPS

    val TRIAL_CHAMBER_INJECT = injectOf(BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE)

    fun init() {
        LootTableEvents.MODIFY.register { key, tableBuilder, _, _ ->
            if (key == BuiltInLootTables.TRIAL_CHAMBERS_REWARD_OMINOUS_RARE)
                tableBuilder.modifyPools {
                    it.add(lootTableReference(registryOps!!.getter(Registries.LOOT_TABLE).get().getOrThrow(TRIAL_CHAMBER_INJECT))
                        .setWeight(2)
                    )
                }
        }
    }
}
package archives.tater.lockedloaded.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagsProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.tags.TagAppender
import net.minecraft.resources.ResourceKey
import net.minecraft.tags.DamageTypeTags
import net.minecraft.tags.TagKey
import net.minecraft.world.damagesource.DamageType
import java.util.concurrent.CompletableFuture

class DamageTagGenerator(
    output: FabricPackOutput,
    registryLookupFuture: CompletableFuture<HolderLookup.Provider>
) : FabricTagsProvider<DamageType>(output, Registries.DAMAGE_TYPE, registryLookupFuture) {

    private fun buildTag(tag: TagKey<DamageType>, block: TagAppender<ResourceKey<DamageType>, DamageType>.() -> Unit) {
        builder(tag).block()
    }

    override fun addTags(registries: HolderLookup.Provider) {
        buildTag(DamageTypeTags.BYPASSES_COOLDOWN) {
            forceAddTag(DamageTypeTags.IS_PROJECTILE)
        }
    }

}
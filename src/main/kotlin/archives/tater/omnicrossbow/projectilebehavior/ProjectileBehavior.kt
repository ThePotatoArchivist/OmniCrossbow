package archives.tater.omnicrossbow.projectilebehavior

import archives.tater.omnicrossbow.projectilebehavior.action.ProjectileAction
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.core.HolderSet
import net.minecraft.core.RegistryCodecs
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.Item

@JvmRecord
data class ProjectileBehavior(
    val items: HolderSet<Item>,
    val projectileAction: ProjectileAction,
) {
    companion object {
        val CODEC: Codec<ProjectileBehavior> = RecordCodecBuilder.create { it.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("items").forGetter(ProjectileBehavior::items),
            ProjectileAction.CODEC.fieldOf("projectile_action").forGetter(ProjectileBehavior::projectileAction)
        ).apply(it, ::ProjectileBehavior) }
    }
}

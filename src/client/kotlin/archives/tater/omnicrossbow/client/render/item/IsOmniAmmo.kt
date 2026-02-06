package archives.tater.omnicrossbow.client.render.item

import archives.tater.omnicrossbow.registry.OmniCrossbowTags
import archives.tater.omnicrossbow.util.isIn
import com.mojang.serialization.MapCodec
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty
import net.minecraft.core.component.DataComponents
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

object IsOmniAmmo : ConditionalItemModelProperty {
    override fun type(): MapCodec<out ConditionalItemModelProperty> = CODEC

    override fun get(
        itemStack: ItemStack,
        level: ClientLevel?,
        owner: LivingEntity?,
        seed: Int,
        displayContext: ItemDisplayContext
    ): Boolean = itemStack.get(DataComponents.CHARGED_PROJECTILES)?.items?.firstOrNull()
        ?.isIn(OmniCrossbowTags.BUILTIN_PROJECTILES) == false

    val CODEC: MapCodec<IsOmniAmmo> = MapCodec.unit(this)
}
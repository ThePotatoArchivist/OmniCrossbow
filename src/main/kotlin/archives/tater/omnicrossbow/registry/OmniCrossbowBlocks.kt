package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.block.HoneySlickBlock
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour

object OmniCrossbowBlocks {
    private fun register(key: ResourceKey<Block>, block: (BlockBehaviour.Properties) -> Block, properties: BlockBehaviour.Properties): Block =
        Registry.register(BuiltInRegistries.BLOCK, key, block(properties.apply {
            setId(key)
        }))

    private fun register(path: String, block: (BlockBehaviour.Properties) -> Block = ::Block, properties: BlockBehaviour.Properties = BlockBehaviour.Properties.of()) =
        register(ResourceKey.create(Registries.BLOCK, OmniCrossbow.id(path)), block, properties)

    private fun register(path: String, block: (BlockBehaviour.Properties) -> Block = ::Block, init: BlockBehaviour.Properties.() -> Unit) =
        register(path, block, BlockBehaviour.Properties.of().apply(init))

    @JvmField
    val HONEY_SLICK = register("honey_slick", ::HoneySlickBlock) {
        noOcclusion()
        strength(0.7f, 0f)
        sound(SoundType.HONEY_BLOCK)
    }

    fun init() {}
}
package archives.tater.omnicrossbow.registry

import archives.tater.omnicrossbow.OmniCrossbow
import archives.tater.omnicrossbow.projectilebehavior.impactaction.BreakBlock
import archives.tater.omnicrossbow.projectilebehavior.impactaction.ImpactAction
import archives.tater.omnicrossbow.projectilebehavior.impactaction.Singleton
import com.mojang.serialization.MapCodec
import net.minecraft.core.Registry
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult

object OmniCrossbowImpactActions {

    private fun register(path: String, codec: MapCodec<out ImpactAction>) {
        Registry.register(OmniCrossbowBuiltinRegistries.IMPACT_ACTION_TYPE, OmniCrossbow.id(path), codec)
    }

    private fun registerSingleton(path: String, singleton: Singleton) = singleton.apply {
        register(path, codec)
    }

    val NONE = registerSingleton("none", object : Singleton() {
        override fun tryImpact(hit: EntityHitResult): Boolean = false
        override fun tryImpact(hit: BlockHitResult): Boolean = false
    })

    init {
        register("break_block", BreakBlock.CODEC)
    }

    fun init() {}
}
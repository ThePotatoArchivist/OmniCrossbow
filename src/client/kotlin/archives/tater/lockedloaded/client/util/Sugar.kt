package archives.tater.lockedloaded.client.util

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey

operator fun <T: Any> FabricRenderState.get(key: RenderStateDataKey<T>): T? = getData(key)
operator fun <T: Any> FabricRenderState.set(key: RenderStateDataKey<T>, value: T?) {
    setData(key, value)
}
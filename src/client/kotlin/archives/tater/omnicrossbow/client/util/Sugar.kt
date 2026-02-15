package archives.tater.omnicrossbow.client.util

import net.fabricmc.fabric.api.client.rendering.v1.FabricRenderState
import net.fabricmc.fabric.api.client.rendering.v1.RenderStateDataKey

operator fun <T: Any> FabricRenderState.get(key: RenderStateDataKey<T>): T? = getData(key)
operator fun <T: Any> FabricRenderState.set(key: RenderStateDataKey<T>, value: T?) {
    setData(key, value)
}

fun <T: Any> FabricRenderState.getOrCreate(key: RenderStateDataKey<T>, create: () -> T): T = get(key) ?: create().also {
    this[key] = it
}
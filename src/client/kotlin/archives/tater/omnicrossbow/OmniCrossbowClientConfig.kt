package archives.tater.omnicrossbow

import folk.sisby.kaleido.api.WrappedConfig
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment

class OmniCrossbowClientConfig : WrappedConfig() {
    @Comment("If custom ammo should be displayed on the crossbow item")
    var renderAmmo = true
}
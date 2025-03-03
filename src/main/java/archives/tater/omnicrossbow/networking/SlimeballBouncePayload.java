package archives.tater.omnicrossbow.networking;

import archives.tater.omnicrossbow.OmniCrossbow;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record SlimeballBouncePayload(int projectileId) implements CustomPayload {
    public static final Identifier IDENTIFIER = OmniCrossbow.id("slimeball_bounce");
    public static final CustomPayload.Id<SlimeballBouncePayload> ID = new CustomPayload.Id<>(IDENTIFIER);
    public static final PacketCodec<RegistryByteBuf, SlimeballBouncePayload> CODEC = PacketCodec.tuple(PacketCodecs.INTEGER, SlimeballBouncePayload::projectileId, SlimeballBouncePayload::new);

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
}

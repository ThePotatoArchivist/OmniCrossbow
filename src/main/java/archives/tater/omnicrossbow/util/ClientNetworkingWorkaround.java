package archives.tater.omnicrossbow.util;

import net.minecraft.network.packet.CustomPayload;

public interface ClientNetworkingWorkaround {
    void send(CustomPayload payload);
}

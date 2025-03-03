package archives.tater.omnicrossbow.util;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

public interface ClientNetworkingWorkaround {
    void send(Identifier channelName, PacketByteBuf buf) throws IllegalStateException;
}

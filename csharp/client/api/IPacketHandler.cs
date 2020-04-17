using com.github.fernthedev.lightchat.core.packets;
using System;
using System.Collections.Generic;
using System.Text;

namespace com.github.fernthedev.lightchat.client.api
{

    public interface IPacketHandler
        {
            public void handlePacket(Packet packet, int packetId);
        }
}

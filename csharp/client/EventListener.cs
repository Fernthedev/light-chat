using com.github.fernthedev.lightchat.core.packets;
using System;
using System.Collections.Generic;
using System.Text;

namespace com.github.fernthedev.lightchat.client
{
    public class EventListener
    {
        private Client client;

        public EventListener(Client client)
        {
            this.client = client;
        }

        public void Received(Packet packet, int packetId)
        {

        }
    }
}

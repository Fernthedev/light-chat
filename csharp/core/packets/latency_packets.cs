namespace com.github.fernthedev.lightchat.core.packets
{
    public interface LatencyPacket
    {

    }

    [PacketInfo("PING_PACKET")]
    public class PingPacket : Packet, LatencyPacket
    {

    }

    [PacketInfo("PING_RECEIVE")]
    public class PingReceive : Packet, LatencyPacket
    {

    }

    [PacketInfo("PONG_PACKET")]
    public class PongPacket : Packet, LatencyPacket
    {

    }
}
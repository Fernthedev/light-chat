using com.github.fernthedev.lightchat.core.encryption;

namespace com.github.fernthedev.lightchat.core.packets
{
    [PacketInfo("HASHED_PASSWORD_PACKET")]
    public class HashedPasswordPacket : Packet
    {
        public HashedPassword hashedPassword { get; }

        public HashedPasswordPacket(string password)
        {
            this.hashedPassword = new HashedPassword(password);
        }

        public HashedPasswordPacket(HashedPassword password)
        {
            this.hashedPassword = password;
        }
    }

    [PacketInfo("ILLEGAL_CONNECTION_PACKET")]
    public class IllegalConnectionPacket : Packet
    {

        public string message { get; }

        public IllegalConnectionPacket(string message)
        {
            this.message = message;
        }
    }


    [PacketInfo("SELF_MESSAGE_PACKET")]
    public class SelfMessagePacket : Packet
    {
        public MessageType type { get; }

        public SelfMessagePacket(MessageType type)
        {
            this.type = type;
        }

        public enum MessageType
        {
            FILL_PASSWORD,
            INCORRECT_PASSWORD_ATTEMPT, // The password attempted is wrong
            INCORRECT_PASSWORD_FAILURE, // The passwords attempted were wrong, so cancelling authentication
            LOST_SERVER_CONNECTION,
            REGISTER_PACKET,
            TIMED_OUT_REGISTRATION
        }
    }
}
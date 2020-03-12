using com.github.fernthedev.lightchat.core.encryption;
using com.github.fernthedev.lightchat.core.util;
using System;
using System.Collections.Generic;
using System.Text;

namespace com.github.fernthedev.lightchat.core.packets
{
    public abstract class Packet : IAcceptablePacketTypes
    {

        [NonSerialized]
        private readonly string packetName;


        public string PacketName => packetName;

        public static string getPacketName(GenericType<Packet> packet)
        {
            if (packet.Equals(typeof(Packet))) throw new ArgumentException("The class cannot be " + typeof(Packet).FullName);

            if (!typeof(Packet).IsAssignableFrom(packet.Typee)) throw new ArgumentException("Packet " + packet.Typee.FullName + " must extend " + typeof(Packet).FullName);

            if (packet.Typee.IsAbstract || packet.Typee.IsInterface) throw new ArgumentException("The class cannot be abstract or interface.");

            if (!System.Attribute.IsDefined(packet.Typee, typeof(PacketInfoAttribute))) throw new ArgumentException("Packet " + packet.Typee.FullName + " must have a packet info annotation");

            return ((PacketInfoAttribute) System.Attribute.GetCustomAttribute(packet.Typee, typeof(PacketInfoAttribute))).Name;
        }
    }

    [System.AttributeUsage(System.AttributeTargets.Class)]
    public sealed class PacketInfoAttribute : System.Attribute
    {
        private string name;

        public string Name => name;

        public PacketInfoAttribute(string name)
        {
            this.name = name;
        }
    }
}

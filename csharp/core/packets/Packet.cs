using com.github.fernthedev.lightchat.core.encryption;
using com.github.fernthedev.lightchat.core.util;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Diagnostics;
using System.Diagnostics.CodeAnalysis;
using System.Text;
using System.Text.Json.Serialization;

namespace com.github.fernthedev.lightchat.core.packets
{
    public abstract class Packet : IAcceptablePacketTypes
    {
        protected Packet()
        {
            if (!Attribute.IsDefined(GetType(), typeof(PacketInfoAttribute))) throw new ArgumentException("Packet " + GetType().FullName + " must have a packet info annotation");

            _PacketName = ((PacketInfoAttribute) Attribute.GetCustomAttribute(GetType(), typeof(PacketInfoAttribute)))?.Name;
        }

        [field: NonSerialized]
        [Newtonsoft.Json.JsonIgnore]
        [JsonIgnore]
        public string _PacketName { get; }

        public static string getPacketName(Type packet)
        {

            if (packet == typeof(Packet)) throw new ArgumentException("The class cannot be " + typeof(Packet).FullName);

            if (!typeof(Packet).IsAssignableFrom(packet)) throw new ArgumentException("Packet " + packet.FullName + " must extend " + typeof(Packet).FullName);

            if (packet.IsAbstract || packet.IsInterface) throw new ArgumentException("The class cannot be abstract or interface.");

            if (!Attribute.IsDefined(packet, typeof(PacketInfoAttribute))) throw new ArgumentException("Packet " + packet.FullName + " must have a packet info annotation");

            return ((PacketInfoAttribute) Attribute.GetCustomAttribute(packet, typeof(PacketInfoAttribute)))?.Name;
        }
    }

    [AttributeUsage(AttributeTargets.Class)]
    public sealed class PacketInfoAttribute : Attribute
    {
        [NotNull]
        public string Name { get; }

        public PacketInfoAttribute(string name)
        {
            Debug.Assert(name != null, nameof(name) + " != null");
            Name = name;
        }
    }
}

using com.github.fernthedev.lightchat.core.packets;
using com.github.fernthedev.lightchat.core.util;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;

namespace com.github.fernthedev.lightchat.core
{
    public class PacketRegistry
    {
        public enum RegisteredReturnValues
        {
            NOT_IN_REGISTRY,
            IN_REGISTRY,
            IN_REGISTRY_DIFFERENT_PACKET
        }

        private PacketRegistry() { }

        private static readonly Dictionary<string, GenericType<Packet>> PACKET_REGISTRY = new Dictionary<string, GenericType<Packet>>();

        public static GenericType<Packet> getPacketClassFromRegistry(string name)
        {


            if (!PACKET_REGISTRY.ContainsKey(name)) throw new InvalidOperationException("The packet registry does not contain packet \"" + name + "\" in the registry. Make sure it is spelled correctly and is case-sensitive.");

            return PACKET_REGISTRY[name];
        }

        public static GenericType<Packet> registerPacket(Packet packet)
        {
            if (PACKET_REGISTRY.ContainsKey(packet.PacketName) && PACKET_REGISTRY[packet.PacketName].Typee != packet.GetType()) throw new InvalidOperationException("The packet " + packet.GetType().FullName + " tried to use packet name \"" + packet.PacketName + "\" which is already taken by the packet " + getPacketClassFromRegistry(packet.PacketName));

            PACKET_REGISTRY.Add(packet.PacketName, new GenericType<Packet>(packet));

            return new GenericType<Packet>(packet);
        }

        public static GenericType<Packet> registerPacket(GenericType<Packet> packet)
        {
            string name = Packet.getPacketName(packet);

            if (PACKET_REGISTRY.ContainsKey(name) && PACKET_REGISTRY[name] != packet) throw new ArgumentException("The packet name \"" + name + "\" is already taken by the packet " + getPacketClassFromRegistry(name));

            PACKET_REGISTRY.Add(name, packet);

            return packet;
        }

        public static RegisteredReturnValues checkIfRegistered(Packet packet)
        {
            if (!PACKET_REGISTRY.ContainsKey(packet.PacketName)) return RegisteredReturnValues.NOT_IN_REGISTRY;

            return PACKET_REGISTRY[packet.PacketName].Typee == new GenericType<Packet>(packet).Typee ? RegisteredReturnValues.IN_REGISTRY : RegisteredReturnValues.IN_REGISTRY_DIFFERENT_PACKET;
        }


        private static Type[] GetTypesInNamespace(string nameSpace)
        {
            return GetTypesInNamespace(Assembly.GetExecutingAssembly(), nameSpace);
        }

            private static Type[] GetTypesInNamespace(Assembly assembly, string nameSpace)
        {
            return
              assembly.GetTypes()
                      .Where(t => t.Namespace.StartsWith(nameSpace, StringComparison.Ordinal) || t.Namespace.Equals(nameSpace, StringComparison.Ordinal))
                      .ToArray();
        }

        public static void registerDefaultPackets()
        {

            registerPacketPackage(GetTypesInNamespace(StaticHandler.PACKET_NAMESPACE));
        }

        public static void registerPacketPackage(Type[] classes)
        {
            GenericType<Packet>[] packetClasses = classes.Where(t => t.IsAssignableFrom(typeof(Packet))).Select(t => new GenericType<Packet>(t)).ToArray();

            foreach (GenericType<Packet> packetClass in packetClasses) {
                StaticHandler.WriteDebug("Registering the class {0}", packetClass.Typee.FullName);
                registerPacket(packetClass);

            }
        }
    }
}

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

        private static readonly Dictionary<string, Type> PACKET_REGISTRY = new Dictionary<string, Type>();

        public static Type getPacketClassFromRegistry(string name)
        {
            if (!PACKET_REGISTRY.ContainsKey(name)) throw new InvalidOperationException("The packet registry does not contain packet \"" + name + "\" in the registry. Make sure it is spelled correctly and is case-sensitive.");

            return PACKET_REGISTRY[name];
        }

        public static GenericType<Packet> registerPacket(Packet packet)
        {
            if (PACKET_REGISTRY.ContainsKey(packet._PacketName) && PACKET_REGISTRY[packet._PacketName] != packet.GetType()) throw new InvalidOperationException("The packet " + packet.GetType().FullName + " tried to use packet name \"" + packet._PacketName + "\" which is already taken by the packet " + getPacketClassFromRegistry(packet._PacketName));

            PACKET_REGISTRY.Add(packet._PacketName, packet.GetType());

            return new GenericType<Packet>(packet);
        }

        public static Type registerPacket(Type packet)
        {
            var name = Packet.getPacketName(packet);

            if (PACKET_REGISTRY.ContainsKey(name) && PACKET_REGISTRY[name] != packet) throw new ArgumentException("The packet name \"" + name + "\" is already taken by the packet " + getPacketClassFromRegistry(name));

            PACKET_REGISTRY.Add(name, packet);

            return packet;
        }

        public static RegisteredReturnValues checkIfRegistered(Packet packet)
        {
            if (packet == null || !PACKET_REGISTRY.ContainsKey(packet._PacketName)) return RegisteredReturnValues.NOT_IN_REGISTRY;

            return PACKET_REGISTRY[packet._PacketName] == packet.GetType() ? RegisteredReturnValues.IN_REGISTRY : RegisteredReturnValues.IN_REGISTRY_DIFFERENT_PACKET;
        }

        public static RegisteredReturnValues checkIfRegistered(string packet)
        {
            if (packet == null || !PACKET_REGISTRY.ContainsKey(packet)) return RegisteredReturnValues.NOT_IN_REGISTRY;

            return RegisteredReturnValues.IN_REGISTRY;
        }



        private static IEnumerable<Type> GetTypesInNamespace(string nameSpace)
        {
            return GetTypesInNamespace(Assembly.GetExecutingAssembly(), nameSpace);
        }

            private static IEnumerable<Type> GetTypesInNamespace(Assembly assembly, string nameSpace)
        {
            return
              assembly.GetTypes()
                      .Where(t => t.Namespace != null && (t.Namespace.StartsWith(nameSpace, StringComparison.Ordinal) || t.Namespace.Equals(nameSpace, StringComparison.Ordinal)))
                      .ToArray();
        }

        public static void registerDefaultPackets()
        {

            registerPacketPackage(GetTypesInNamespace(StaticHandler.PACKET_NAMESPACE));
        }

        public static void registerPacketPackage(IEnumerable<Type> classes)
        {
            var packetClasses = classes.Where(t => typeof(Packet).IsAssignableFrom(t)).ToArray();

            foreach (var packetClass in packetClasses) {
                StaticHandler.Core.Logger.Debug("Registering the class {0}", packetClass.FullName);
                if (packetClass == typeof(Packet)) continue;

                registerPacket(packetClass);

            }
        }
    }
}

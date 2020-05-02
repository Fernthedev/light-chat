using com.github.fernthedev.lightchat.core.packets;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Text;

namespace com.github.fernthedev.lightchat.core.encryption
{

    public interface IAcceptablePacketTypes
    {

    }

    public class GenericJsonPacketWrapper : PacketWrapper<object>
    {
        public GenericJsonPacketWrapper(object jsonObject, string packetIdentifier, int packetId) : base(jsonObject, packetIdentifier, packetId)
        {
        }

        [JsonConstructor]
        protected GenericJsonPacketWrapper(string jsonObject, string packetIdentifier, int packetId) : base(jsonObject, packetIdentifier, packetId)
        {
        }
    }

    public class PacketWrapper<T>
    {
        [JsonProperty(Required = Required.Always, PropertyName = "ENCRYPT")]
        public bool ENCRYPT {get; protected set; }

        [JsonProperty(Required = Required.Always, PropertyName = "jsonObject")]
        public string jsonObject { get; private set; }

        [JsonProperty(Required = Required.Always, PropertyName = "packetId")]
        public int packetId { get; private set; }


        [NonSerialized]
        public readonly T _jsonObjectInstance;

        public string packetIdentifier { get; }

        public PacketWrapper(T jsonObject, string packetIdentifier, int packetId)
        {
            this.packetIdentifier = packetIdentifier;
            this._jsonObjectInstance = jsonObject;
            this.jsonObject = StaticHandler.defaultJsonHandler.toJson(jsonObject);
            this.packetId = packetId;
            
        }

        protected PacketWrapper(string jsonObject, string packetIdentifier, int packetId)
        {
            this.packetIdentifier = packetIdentifier;
            this.jsonObject = jsonObject;
            this.packetId = packetId;
        }
    }

    public class EncryptedPacketWrapper : PacketWrapper<EncryptedBytes>
    {
        public EncryptedPacketWrapper(EncryptedBytes jsonObject, Packet packet, int packetId) : base(jsonObject, packet?._PacketName, packetId)
        {
            StaticHandler.Core.Logger.Debug("Json Object is {0}", this.jsonObject);
            ENCRYPT = true;
            if (PacketRegistry.checkIfRegistered(packet) == PacketRegistry.RegisteredReturnValues.NOT_IN_REGISTRY)
            {
                throw new InvalidOperationException("The packet trying to be wrapped is not registered. \"" + packet.GetType().FullName + "\"");
            }
        }


        [JsonConstructor]
        public EncryptedPacketWrapper(string jsonObject, string packetIdentifier, int packetId) : base(jsonObject, packetIdentifier, packetId)
        {
            StaticHandler.Core.Logger.Debug("Json Object is {0}", this.jsonObject);
            ENCRYPT = true;
            if (PacketRegistry.checkIfRegistered(packetIdentifier) == PacketRegistry.RegisteredReturnValues.NOT_IN_REGISTRY)
            {
                throw new InvalidOperationException("The packet trying to be wrapped is not registered. \"" + packetIdentifier + "\"");
            }
        }
    }

    public class UnencryptedPacketWrapper : PacketWrapper<Packet>, IAcceptablePacketTypes
    {
        public UnencryptedPacketWrapper(Packet packet, int packetId) : base(packet, packet?._PacketName, packetId)
        {

            if (packet == null) throw new ArgumentNullException(nameof(packet));

            ENCRYPT = false;

            if (PacketRegistry.checkIfRegistered(packet) == PacketRegistry.RegisteredReturnValues.NOT_IN_REGISTRY)
            {
                throw new InvalidOperationException("The packet trying to be wrapped is not registered. \"" +
                                                    packet.GetType().FullName + "\"");
            }

        }

        [JsonConstructor]
        protected UnencryptedPacketWrapper(string jsonObject, string packetIdentifier, int packetId) : base(jsonObject, packetIdentifier, packetId)
        {

        }




    }
}

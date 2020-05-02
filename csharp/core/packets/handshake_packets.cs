using System;
using System.Collections.Generic;
using System.Diagnostics.CodeAnalysis;
using System.Linq;
using System.Security.Cryptography;
using System.Security.Cryptography.X509Certificates;
using System.Text.Json.Serialization;
using com.github.fernthedev.lightchat.core.data;
using com.github.fernthedev.lightchat.core.encryption;
using Newtonsoft.Json;

namespace com.github.fernthedev.lightchat.core.packets
{
    /**
    * The final packet sent in the handshake
    */
    [PacketInfo("CONNECTED_PACKET")]
    public class ConnectedPacket : Packet
    {
        [NotNull]
        [JsonProperty]
        public string name { get; }

        [JsonProperty]
        [NotNull]
        public string os { get; }

        [JsonProperty]
        [NotNull]
        public string langFramework { get; }

        [JsonProperty]
        [NotNull]
        public VersionDataString versionData;

//    @NonNull
//    private UUID uuid;

        public ConnectedPacket([NotNull] string name, [NotNull] string os, VersionData versionData, string langFramework) { //, @NonNull UUID uuid) {
            this.name = name;
            this.os = os;
            this.versionData = new VersionDataString(version: versionData.Version.ToString(), minVersion: versionData.MinVersion.ToString());
            this.langFramework = langFramework;
//        this.uuid = uuid;
        }
    }

    [PacketInfo("INITIAL_HANDSHAKE_PACKET")]
    public class InitialHandshakePacket : Packet
    {

        public InitialHandshakePacket(RSACryptoServiceProvider publicKey, VersionData versionData)
        {
            this.publicKey = RSAEncryptionUtil.toBase64(publicKey);
            this.versionData = new VersionDataString(version: versionData.Version.ToString(),
                minVersion: versionData.MinVersion.ToString());
        }

        [JsonConstructor]
        public InitialHandshakePacket(string publicKey, VersionData versionData)
        {
            this.publicKey = publicKey;
            this.versionData = new VersionDataString(version: versionData.Version.ToString(),
                minVersion: versionData.MinVersion.ToString());
        }

        private string publicKey;

        private VersionDataString versionData;

        public VersionData getVersionData()
        {
            return new VersionData(versionData.version, versionData.minVersion);
        }

        public RSACryptoServiceProvider getPublicKey()
        {
            return RSAEncryptionUtil.toPublicKey(publicKey);
        }

    }


    [PacketInfo("KEY_RESPONSE_PACKET")]
    public class KeyResponsePacket : Packet
    {

        public KeyResponsePacket(AesCryptoServiceProvider secretKey, RSACryptoServiceProvider publicKey)
        {
            this.secretKeyEncrypted = RSAEncryptionUtil.encryptKey(secretKey, publicKey).ToList();
        }

        [JsonProperty(Required = Required.Always)]
        public List<byte> secretKeyEncrypted { get; private set; }
    }

    [PacketInfo("REQUEST_CONNECT_INFO_PACKET")]
    public class RequestConnectInfoPacket : Packet {

    }

}
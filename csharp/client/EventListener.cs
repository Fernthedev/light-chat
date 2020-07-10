using com.github.fernthedev.lightchat.core.packets;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using com.github.fernthedev.lightchat.client.events;
using com.github.fernthedev.lightchat.core;
using com.github.fernthedev.lightchat.core.data;
using com.github.fernthedev.lightchat.core.encryption;
using com.github.fernthedev.lightchat.core.exceptions;
using com.github.fernthedev.lightchat.core.util;
using Org.BouncyCastle.Security;

namespace com.github.fernthedev.lightchat.client
{
    public class PacketEventListener
    {
        private LightClient lightClient;

        public PacketEventListener(LightClient lightClient)
        {
            this.lightClient = lightClient;
        }

        public void Received(Packet p, int packetId)
        {
            try
            {
                if (p is PingPacket)
                {
                    lightClient.startPingStopwatch();

                    lightClient.sendObject(new PongPacket(), false);
                }
                else if (p is PingReceive)
                {

                    lightClient.endPingStopwatch();


                    lightClient.LoggerInstance.Debug("Ping: {0}", lightClient.getPingTime().Milliseconds + "ms");

                }
                else if (p is IllegalConnectionPacket)
                {
                    lightClient.LoggerInstance.Info(((IllegalConnectionPacket) p).message);
                    lightClient.disconnect(ServerDisconnectEvent.DisconnectStatus.CONNECTION_LOST);
                }
                else if (p is InitialHandshakePacket)
                {
                    // Handles object encryption key sharing
                    var packet = (InitialHandshakePacket) p;

                    var versionData = packet.getVersionData();

                    var versionRange = StaticHandler.getVersionRangeStatus(versionData);

                    if (versionRange == StaticHandler.VersionRange.MATCH_REQUIREMENTS)
                        lightClient.LoggerInstance.Info("Version range requirements match Server version.");
                    else
                    {
                        // Current version is smaller than the server's required minimum
                        if (versionRange == StaticHandler.VersionRange.WE_ARE_LOWER)
                        {
                            lightClient.LoggerInstance
                                .Info(
                                    "The client version ({0}) does not meet server's minimum version ({1}) requirements. Expect incompatibility issues",
                                    StaticHandler.VERSION_DATA.Version.ToString(), versionData.MinVersion.ToString());
                        }

                        // Current version is larger than server's minimum version
                        if (versionRange == StaticHandler.VersionRange.WE_ARE_HIGHER)
                        {
                            lightClient.LoggerInstance
                                .Info(
                                    "The server version ({0}) does not meet client's minimum version ({1}) requirements. Expect incompatibility issues",
                                    versionData.Version.ToString(), StaticHandler.VERSION_DATA.MinVersion.ToString());
                        }

                    }

                    var secretKey = EncryptionUtil.generateAESProvider();

                    // TODO: Remove this debug
                    string debug = "Using key " + string.Join( ",", secretKey.Key) + " base64:" + Convert.ToBase64String(secretKey.Key);
                    StaticHandler.Core.Logger.Debug(debug);

                    lightClient.setSecretKey(secretKey);


                    var responsePacket = new KeyResponsePacket(secretKey, packet.getPublicKey());

                    lightClient.sendObject(responsePacket, false);


                    lightClient.callEvent(new ServerConnectHandshakeEvent(lightClient.Channel));
                }
                else if (p is RequestConnectInfoPacket)
                {
                    var connectedPacket = lightClient.buildConnectedPacket();

                    lightClient.sendObject(connectedPacket).ConfigureAwait(false);
                    StaticHandler.Core.Logger.Info("Sent the connection Packet for request");

                }
                else if (p is SelfMessagePacket)
                {
                    switch (((SelfMessagePacket) p).type)
                    {
                        case SelfMessagePacket.MessageType.TIMED_OUT_REGISTRATION:
                            lightClient.LoggerInstance.Info("Timed out on registering.");
                            lightClient.disconnect(ServerDisconnectEvent.DisconnectStatus.TIMEOUT);
                            break;

                        case SelfMessagePacket.MessageType.REGISTER_PACKET:
                            lightClient.registered = true;
                            lightClient.LoggerInstance.Info("Successfully connected to server");
                            lightClient.callEvent(new ServerConnectFinishEvent(lightClient.Channel));
                            break;

                        case SelfMessagePacket.MessageType.LOST_SERVER_CONNECTION:
                            lightClient.LoggerInstance.Info("Lost connection to server! Must have shutdown!");
                            lightClient.disconnect(ServerDisconnectEvent.DisconnectStatus.CONNECTION_LOST);
                            break;
                    }

                }

                foreach (var iPacketHandler in lightClient.getPacketHandlers())
                {
                    Task.Run(() =>
                    {
                        try
                        {
                            iPacketHandler.handlePacket(p, packetId);
                        }
                        catch (Exception e)
                        {
                            throw ExceptionUtil.throwParsePacketException(e, p);
                        }
                    });
                }
            }
            catch (ParsePacketException ignored)
            {
                throw;
            }
            catch (Exception e)
            {
                throw ExceptionUtil.throwParsePacketException(e, p);
            }
        }
    }
}

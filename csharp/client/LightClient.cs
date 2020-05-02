using com.github.fernthedev.lightchat.core;
using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using com.github.fernthedev.lightchat.core.packets;
using com.github.fernthedev.lightchat.core.codecs;
using System.Collections.Generic;
using System.Diagnostics;
using com.github.fernthedev.lightchat.core.encryption;
using System.Threading.Tasks;
using DotNetty.Transport.Bootstrapping;
using DotNetty.Transport.Channels;
using DotNetty.Transport.Channels.Sockets;
using DotNetty.Codecs;
using com.github.fernthedev.lightchat.core.util;
using System.Security.Cryptography;
using com.github.fernthedev.lightchat.client.api;
using com.github.fernthedev.lightchat.client.events;
using com.github.fernthedev.lightchat.core.events;
using DotNetty.Handlers.Logging;

namespace com.github.fernthedev.lightchat.client
{
    public class ClientSettings : CoreSettings
    {
    }

    public class Client : IEncryptionKeyHolder
    {
        private string host;
        private int port;

        public string Name { get; set; } = Environment.MachineName;

        private PacketEventListener eventListener;

        private ClientSettings settings { get; set; } = new ClientSettings();

        private IEventLoopGroup group;

        private ClientHandler clientHandler;
        private PacketEventListener listener;

        public bool registered { get; internal set; }

        public IChannel Channel { get; private set; }

        private AesCryptoServiceProvider secretKey;
        private ICryptoTransform encryptor, decryptor;

        private readonly List<IPacketHandler> packetHandlers = new List<IPacketHandler>();
        private Stopwatch stopwatch = new Stopwatch();

        internal void startPingStopwatch() {
            stopwatch.Reset();
            stopwatch.Start();
        }

        internal void endPingStopwatch() {
            stopwatch.Stop();
        }

        public TimeSpan getPingTime() {
            return stopwatch.Elapsed;
        }

        private int MaxPacketId { get; set; } = StaticHandler.DEFAULT_PACKET_ID_MAX;

        public void addPacketHandler(IPacketHandler handler)
        {
            packetHandlers.Add(handler);
        }

        public void removeHandler(IPacketHandler handler)
        {
            packetHandlers.Remove(handler);
        }

        public List<IPacketHandler> getPacketHandlers()
        {
            return new List<IPacketHandler>(packetHandlers);
        }

        public event EventHandler<IEvent> eventHandler;

        /**
         * Packet:[ID,lastPacketSentTime]
         */
        private readonly Dictionary<GenericType<Packet>, Tuple<int, long>> packetIdMap =
            new Dictionary<GenericType<Packet>, Tuple<int, long>>();

        public static Logger Logger => StaticHandler.Core.Logger;

        public Logger LoggerInstance => Logger;

        public Client(string host, int port)
        {

            StaticHandler.Core = new ClientCore();

            this.host = host;
            this.port = port;

            listener = new PacketEventListener(this);
            clientHandler = new ClientHandler(this, listener);
        }

        public void reInitialize(string host, int port)
        {
            this.host = host;
            this.port = port;
        }

        public async Task Connect()
        {
            registered = false;
            group = new MultithreadEventLoopGroup();


            StaticHandler.Core.Logger.Info("Connecting to server. IP: " + host);

            var remoteEp = new IPEndPoint(IPAddress.Parse(host), port);


            var bootstrap = new Bootstrap();

            bootstrap.Group(group)
                .Channel<TcpSocketChannel>()
                .Option(ChannelOption.TcpNodelay, true)
                // .Option(ChannelOption.SoKeepalive, true)
                // .Option(ChannelOption.ConnectTimeout, TimeSpan.FromMilliseconds(settings.TimeoutMS))
                .Handler(new ActionChannelInitializer<IChannel>(channel =>
                {
                    var pipeLine = channel.Pipeline;

                    pipeLine.AddLast(new LoggingHandler(LogLevel.INFO));

                    pipeLine.AddLast("frameDecoder", new LineBasedFrameDecoder(StaticHandler.LineLimit));
                    pipeLine.AddLast("jsonDecoder", new EncryptedJSONDecoder(this, settings.jsonCodec));
                    pipeLine.AddLast("jsonEncoder", new EncryptedJSONEncoder(this, settings.jsonCodec));


                    pipeLine.AddLast(clientHandler);
                }));


            var futureChannel = bootstrap.ConnectAsync(remoteEp);

            Channel = await futureChannel.ConfigureAwait(false);



            if (futureChannel.IsCompletedSuccessfully || Channel.Open || Channel.Active || Channel.Registered)
            {
                StaticHandler.Core.Logger.Info("Successfully connected");
            }
            else
            {
                StaticHandler.Core.Logger.Error("Unable to connect");
            }


            // Create a TCP/IP socket.  
            /*            client = new Socket(ipAddress.AddressFamily,
                            SocketType.Stream, protocolType);

                        // Connect to the remote endpoint.  
                        client.BeginConnect(remoteEP,
                            new AsyncCallback(ConnectCallback), client);
                        connectDone.WaitOne();

                        // Receive the response from the remote device.  
                        Receive(client);
                        receiveDone.WaitOne();*/

            // Write the response to the console.  
            /*            Console.WriteLine("Response received : {0}", response);*/
        }

        public async Task disconnect(
            ServerDisconnectEvent.DisconnectStatus status = ServerDisconnectEvent.DisconnectStatus.DISCONNECTED)
        {
            registered = false;

            await Channel.CloseAsync().ConfigureAwait(true);

            callEvent(new ServerDisconnectEvent(Channel, status));

            await group.ShutdownGracefullyAsync(TimeSpan.FromMilliseconds(100), TimeSpan.FromSeconds(1))
                .ConfigureAwait(true);
        }

        public async Task sendObject(Packet packet, bool encrypt = true)
        {
            var packetIdPair = UpdatePacketIdPair(new GenericType<Packet>(packet.GetType()), -1);

            if (packetIdPair.Item1 > MaxPacketId || JavaUtil.CurrentTimeMillis() - packetIdPair.Item2 > 900) UpdatePacketIdPair(new GenericType<Packet>( packet.GetType()), 0);

            if (encrypt)
            {
                await Channel.WriteAndFlushAsync(packet).ConfigureAwait(true);
            }
            else
            {
                await Channel.WriteAndFlushAsync(new UnencryptedPacketWrapper(packet,
                    -1 /* TODO: Insert packet id implemantion (Follow Java implementation)*/)).ConfigureAwait(true);
            }
        }

        private Tuple<int, long> UpdatePacketIdPair(GenericType<Packet> packet, int newId)
        {
            var packetIdPair = getPacketId(packet, null, null);

            if (packetIdPair == null)
                packetIdPair = new Tuple<int, long>(0, JavaUtil.CurrentTimeMillis());
            else
            {
                if (newId == -1) newId = packetIdPair.Item1 + 1;
                packetIdPair = new Tuple<int, long>(newId, JavaUtil.CurrentTimeMillis());
            }

            packetIdMap.Add(packet, packetIdPair);
            return packetIdPair;
        }


        public AesCryptoServiceProvider getSecretKey(IChannelHandlerContext ctx, IChannel channel)
        {
            return secretKey;
        }

        public ICryptoTransform getSecretKeyEncryptor(IChannelHandlerContext ctx, IChannel channel)
        {
            return encryptor;
        }

        public ICryptoTransform getSecretKeyDecryptor(IChannelHandlerContext ctx, IChannel channel)
        {
            return decryptor;
        }

        public bool isEncryptionKeyRegistered(IChannelHandlerContext ctx, IChannel channel)
        {
            return secretKey != null;
        }

        public Tuple<int, long> getPacketId(GenericType<Packet> clazz, IChannelHandlerContext ctx, IChannel channel)
        {
            return packetIdMap[clazz];
        }

        public T callEvent<T>(T e) where T : IEvent
        {
            eventHandler?.Invoke(this, e);

            return e;
        }

        public ConnectedPacket buildConnectedPacket()
        {
            return new ConnectedPacket(Name, Environment.OSVersion.ToString(), StaticHandler.VERSION_DATA);
        }

        public void setSecretKey(AesCryptoServiceProvider aesCryptoServiceProvider)
        {
            secretKey = aesCryptoServiceProvider;
            encryptor = aesCryptoServiceProvider.CreateEncryptor();
            decryptor = aesCryptoServiceProvider.CreateDecryptor();
        }
    }
}
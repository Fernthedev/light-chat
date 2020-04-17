using com.github.fernthedev.lightchat.core;
using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using com.github.fernthedev.lightchat.core.packets;
using com.github.fernthedev.lightchat.core.codecs;
using System.Collections.Generic;
using com.github.fernthedev.lightchat.core.encryption;
using System.Threading.Tasks;
using DotNetty.Transport.Bootstrapping;
using DotNetty.Transport.Channels;
using DotNetty.Transport.Channels.Sockets;
using DotNetty.Codecs;
using com.github.fernthedev.lightchat.core.util;
using System.Security.Cryptography;
using com.github.fernthedev.lightchat.client.events;
using com.github.fernthedev.lightchat.core.events;

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

        public ClientSettings settings { get; set; } = new ClientSettings();

        private IEventLoopGroup group;

        private ClientHandler clientHandler;
        private PacketEventListener listener;

        private IChannel channel;

        private RijndaelManaged secretKey;

        public event EventHandler<IEvent> eventHandler;

        /**
         * Packet:[ID,lastPacketSentTime]
         */
    private readonly Dictionary<GenericType<Packet>, Tuple<int, long>> packetIdMap = new Dictionary<GenericType<Packet>, Tuple<int, long>>();

        public Client(string host, int port)
        {
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

            group = new MultithreadEventLoopGroup();



            StaticHandler.core.logger.Info("Connecting to server.");

            // Establish the remote endpoint for the socket.  
            IPHostEntry ipHostInfo = Dns.GetHostEntry(host);
            IPAddress ipAddress = ipHostInfo.AddressList[0];
            IPEndPoint remoteEP = new IPEndPoint(ipAddress, port);



            var bootstrap = new Bootstrap();

            bootstrap.Group(group)
                .Channel<TcpSocketChannel>()
                .Option(ChannelOption.TcpNodelay, true)
                .Option(ChannelOption.SoKeepalive, true)
                .Option(ChannelOption.ConnectTimeout, TimeSpan.FromMilliseconds(settings.TimeoutMS))
                .Handler(new ActionChannelInitializer<ISocketChannel>(channel =>
                {
                    IChannelPipeline pipeLine = channel.Pipeline;

                    pipeLine.AddLast("frameDecoder", new LineBasedFrameDecoder(StaticHandler.LineLimit));
                    pipeLine.AddLast("jsonDecoder", new EncryptedJSONDecoder(this, settings.jsonCodec));
                    pipeLine.AddLast("jsonEncoder", new EncryptedJSONEncoder(this, settings.jsonCodec));



                    pipeLine.AddLast(clientHandler);
                }));


            channel = await bootstrap.ConnectAsync(remoteEP).ConfigureAwait(true);

            if (channel.Open)
            {
                StaticHandler.core.logger.Info("Sucessfully connected");
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

        public async Task disconnect(ServerDisconnectEvent.DisconnectStatus status = ServerDisconnectEvent.DisconnectStatus.DISCONNECTED)
        {
            
            await channel.CloseAsync().ConfigureAwait(true);

            callEvent(new ServerDisconnectEvent(channel, status));

            await group.ShutdownGracefullyAsync(TimeSpan.FromMilliseconds(100), TimeSpan.FromSeconds(1)).ConfigureAwait(true);
        }

        public async Task send(Packet packet, bool encrypt = true)
        {
            List<object> outList = new List<object>();
            if (encrypt)
            {
                await channel.WriteAndFlushAsync(packet).ConfigureAwait(true);

            }
            else
            {
                await channel.WriteAndFlushAsync(new UnencryptedPacketWrapper(packet, -1 /* TODO: Insert packet id implemantion (Follow Java implementation)*/)).ConfigureAwait(true);
            }
        }

        private Tuple<int, long> UpdatePacketIdPair(GenericType<Packet> packet, int newId)
        {
            Tuple<int, long> packetIdPair = getPacketId(packet, null, null);

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


        public RijndaelManaged getSecretKey(IChannelHandlerContext ctx, IChannel channel)
        {
            return secretKey;
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
    }
}

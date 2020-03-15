﻿using com.github.fernthedev.lightchat.core;
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

        private EventListener eventListener;

        public ClientSettings settings { get; set; } = new ClientSettings();

        private IEventLoopGroup group;

        private ClientHandler clientHandler;
        private EventListener listener;

        private IChannel channel;

        public Client(string host, int port)
        {
            this.host = host;
            this.port = port;

            listener = new EventListener(this);
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

        public async Task disconnect()
        {
            await channel.CloseAsync().ConfigureAwait(true);

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

        private static void ConnectCallback(IAsyncResult ar)
        {
            try
            {
                // Retrieve the socket from the state object.  
                Socket client = (Socket)ar.AsyncState;

                // Complete the connection.  
                client.EndConnect(ar);

                Console.WriteLine("Socket connected to {0}",
                    client.RemoteEndPoint.ToString());

                // Signal that the connection has been made.  
                connectDone.Set();
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }

        private static void Receive(Socket client)
        {
            try
            {
                // Create the state object.  
                StateObject state = new StateObject();
                state.workSocket = client;

                // Begin receiving the data from the remote device.  
                client.BeginReceive(state.buffer, 0, StateObject.BufferSize, 0,
                    new AsyncCallback(ReceiveCallback), state);
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }

        private static void ReceiveCallback(IAsyncResult ar)
        {
            try
            {
                // Retrieve the state object and the client socket   
                // from the asynchronous state object.  
                StateObject state = (StateObject)ar.AsyncState;
                Socket client = state.workSocket;

                // Read data from the remote device.  
                int bytesRead = client.EndReceive(ar);

                if (bytesRead > 0)
                {
                    // There might be more data, so store the data received so far.  
                    state.sb.Append(Encoding.ASCII.GetString(state.buffer, 0, bytesRead));

                    // Get the rest of the data.  
                    client.BeginReceive(state.buffer, 0, StateObject.BufferSize, 0,
                        new AsyncCallback(ReceiveCallback), state);
                }
                else
                {
                    // All the data has arrived; put it in response.  
                    if (state.sb.Length > 1)
                    {
                        response = state.sb.ToString();
                    }
                    // Signal that all bytes have been received.  
                    receiveDone.Set();
                }
            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }

        private void Write(byte[] data)
        {
            byte[] byteData;
            if (data is byte[])
            {
                byteData = data as byte[];
            }
            else
            {
                throw new InvalidCastException("Data must be string or byte[]");
            }

            // Begin sending the data to the remote device.  
            client.BeginSend(byteData, 0, byteData.Length, 0,
                new AsyncCallback(SendCallback), client);
        }

        private static void SendCallback(IAsyncResult ar)
        {
            try
            {
                // Retrieve the socket from the state object.  
                Socket client = (Socket)ar.AsyncState;

                // Complete sending the data to the remote device.  
                int bytesSent = client.EndSend(ar);
                Console.WriteLine("Sent {0} bytes to server.", bytesSent);

                // Signal that all bytes have been sent.  
                sendDone.Set();

            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }
        }

        public object getSecretKey(IChannelHandlerContext ctx, IChannel channel)
        {
            throw new NotImplementedException();
        }

        public bool isEncryptionKeyRegistered(IChannelHandlerContext ctx, IChannel channel)
        {
            throw new NotImplementedException();
        }

        public Tuple<int, long> getPacketId<T>(GenericType<T> clazz, IChannelHandlerContext ctx, IChannel channel) where T : Packet
        {
            throw new NotImplementedException();
        }
    }
}

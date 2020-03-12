﻿using com.github.fernthedev.lightchat.core;
using System;
using System.Net;
using System.Net.Sockets;
using System.Text;
using System.Threading;
using com.github.fernthedev.lightchat.core.packets;
using com.github.fernthedev.lightchat.core.codecs;
using System.Collections.Generic;
using System.Collections;
using com.github.fernthedev.lightchat.core.encryption;

namespace com.github.fernthedev.lightchat.client
{
    // State object for receiving data from remote device.  
    public class StateObject
    {
        // Client socket.  
        public Socket workSocket = null;
        // Size of receive buffer.  
        public const int BufferSize = 256;
        // Receive buffer.  
        public byte[] buffer = new byte[BufferSize];
        // Received data string.  
        public StringBuilder sb = new StringBuilder();
    }


    public class Client
    {
        private string host;
        private int port;

        private string Name {
            get;
            set;
        } = Environment.MachineName;

        private ProtocolType protocolType = ProtocolType.Tcp;
        private EventListener eventListener;

        // ManualResetEvent instances signal completion.  
        private static ManualResetEvent connectDone =
            new ManualResetEvent(false);
        private static ManualResetEvent sendDone =
            new ManualResetEvent(false);
        private static ManualResetEvent receiveDone =
            new ManualResetEvent(false);

        // The response from the remote device.  
        private static String response = String.Empty;
        private Socket client;

        private Encoder<IAcceptablePacketTypes> encoder { get; set; }
        private Decoder<Packet> decoder { get; set; }

        public Client(string host, int port)
        {
            this.host = host;
            this.port = port;
        }

        public void reInitialize(string host, int port)
        {
            this.host = host;
            this.port = port;
        }

        private void connect()
        {
            StaticHandler.WriteInfo("Connecting to server.");

            // Establish the remote endpoint for the socket.  
            IPHostEntry ipHostInfo = Dns.GetHostEntry(host);
            IPAddress ipAddress = ipHostInfo.AddressList[0];
            IPEndPoint remoteEP = new IPEndPoint(ipAddress, port);

            // Create a TCP/IP socket.  
            client = new Socket(ipAddress.AddressFamily,
                SocketType.Stream, protocolType);

            // Connect to the remote endpoint.  
            client.BeginConnect(remoteEP,
                new AsyncCallback(ConnectCallback), client);
            connectDone.WaitOne();

            // Receive the response from the remote device.  
            Receive(client);
            receiveDone.WaitOne();

            // Write the response to the console.  
            Console.WriteLine("Response received : {0}", response);
        }

        public void send(Packet packet, bool encrypt = true)
        {
            List<object> outList = new List<object>();
            if (encrypt)
            {
                encoder.Encode(packet, outList);
            } else {
                encoder.Encode(new UnencryptedPacketWrapper(packet, -1 /* TODO: Insert packet id implemantion (Follow Java implementation)*/), outList);
            }
           
            outList.ForEach(o => Write(o as byte[]));
        }

        public void close()
        {
            // Release the socket.  
            client.Shutdown(SocketShutdown.Both);
            client.Close();
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
            } else
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
    }
}
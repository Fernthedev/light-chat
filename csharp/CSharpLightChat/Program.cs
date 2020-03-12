using System;
using System.Globalization;
using com.github.fernthedev.lightchat.core;

namespace com.github.fernthedev.lightchat.client
{
    /**
     * This starts the program
     */
    class MainClient
    {
        static void Main(string[] args)
        {
            string host = null;
            int port = -1;

            for (int i = 0; i < args.Length; i++)
            {
                String arg = args[i];

                if (arg.Equals("-port"))
                {
                    try
                    {
                        port = int.Parse(args[i + 1], CultureInfo.CurrentCulture);
                        if (port < 0)
                        {
                            StaticHandler.WriteError("-port cannot be less than 0");
                            port = -1;
                        }
                        else StaticHandler.WriteInfo("Using port {0}", args[i + +1]);
                    }
                    catch (Exception ex)
                    {
                        if (ex is FormatException || ex is IndexOutOfRangeException)
                        {
                            StaticHandler.WriteError("-port is not a number");
                            port = -1;
                        }
                        else throw;
                    }


                    if (arg.Equals("-ip") || arg.Equals("-host"))
                    {
                        try
                        {
                            host = args[i + 1];
                            StaticHandler.WriteError("Using host {0}", args[i + +1]);
                        }
                        catch (IndexOutOfRangeException)
                        {
                            StaticHandler.WriteError("Cannot find argument for -host");
                            host = null;
                        }
                    }

                    if (arg.Equals("-debug"))
                    {
                        StaticHandler.Debug = true;
                        StaticHandler.WriteDebug("Debug enabled");
                    }
                }
            }

            while (host == null || host.Equals("") || port == -1)
            {
                if (host == null || host.Equals(""))
                    host = readLine("Host:");

                if (port == -1)
                    port = readInt("Port:");
            }

            Client client = new Client(host, port);
        }

        private static int readInt(string v)
        {
            StaticHandler.WriteInfo(v);
            string re = Console.ReadLine();

            if (!int.TryParse(re, out int retu))
            {
                StaticHandler.WriteWarn("Input is not numeric");
                return readInt(v);
            }

            return retu;
        }

        private static string readLine(string v)
        {
            StaticHandler.WriteInfo(v);
            return Console.ReadLine();
        }
    }
}

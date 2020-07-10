using System;
using System.Globalization;
using System.Threading;
using com.github.fernthedev.lightchat.client;
using com.github.fernthedev.lightchat.core;

namespace com.github.fernthedev.lightchat.terminal.client
{
    /**
     * This starts the program
     */
    internal static class Program
    {

        private static readonly Logger logger = new Logger();

        private static int Main(string[] args)
        {
            var running = true;

            string host = null;
            var port = -1;

            for (var i = 0; i < args.Length; i++)
            {
                var arg = args[i];

                if (arg.Equals("-port"))
                {
                    try
                    {
                        port = int.Parse(args[i + 1], CultureInfo.CurrentCulture);
                        if (port < 0)
                        {
                            logger.Error("-port cannot be less than 0");
                            port = -1;
                        }
                        else logger.Info("Using port {0}", args[i + +1]);
                    }
                    catch (Exception ex)
                    {
                        if (ex is FormatException || ex is IndexOutOfRangeException)
                        {
                            logger.Error("-port is not a number");
                            port = -1;
                        }
                        else throw;
                    }
                }


                if (arg.Equals("-ip") || arg.Equals("-host"))
                {
                    try
                    {
                        host = args[i + 1];
                        logger.Info("Using host {0}", args[i + +1]);
                    }
                    catch (IndexOutOfRangeException)
                    {
                        logger.Error("Cannot find argument for -host");
                        host = null;
                    }
                }


                if (arg.Equals("-debug"))
                {
                    StaticHandler.Debug = true;
                    logger.Debug("Debug enabled");
                }
            }



            while (host == null || host.Equals("") || port == -1)
            {
                if (host == null || host.Equals(""))
                    host = readLine("Host:");

                if (port == -1)
                    port = readInt("Port:");
            }


            var client = new LightClient(host, port);
            client.Connect().ConfigureAwait(false).GetAwaiter().OnCompleted(() =>
            {
                logger.Info("Finisehd");
            });

            while (client.running)
            {
                Thread.Sleep(10);
            }

            logger.Warn("Exiting for unknown reason");

            return -1;
        }

        private static int readInt(string v)
        {
            while (true)
            {
                logger.Info(v);
                var re = Console.ReadLine();

                if (int.TryParse(re, out var retu)) return retu;

                logger.Warn("Input is not numeric");
            }
        }

        private static string readLine(string v)
        {
            logger.Info(v);
            return Console.ReadLine();
        }
    }
}

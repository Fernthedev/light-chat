using com.github.fernthedev.lightchat.core.packets;
using System;
using System.Collections.Generic;
using System.Text;

namespace com.github.fernthedev.lightchat.core
{
    public static class StaticHandler
    {
        public static bool Debug { get; set; } = false;
        public static readonly string PACKET_NAMESPACE = typeof(Packet).Namespace;

        public static void WriteInfo(string msg, params string[] objects)
        {
            Console.WriteLine("[Info] " + msg, objects);
        }

        public static void WriteError(string msg, params string[] objects)
        {
            Console.Error.WriteLine("[Error] " + msg, objects);
        }

        public static void WriteWarn(string msg, params string[] objects)
        {
            Console.WriteLine("[Warn] " + msg, objects);
        }

        public static void WriteDebug(string msg, params string[] objects)
        {
            if (Debug) Console.WriteLine("[DEBUG] " + msg, objects);
        }

    }
}

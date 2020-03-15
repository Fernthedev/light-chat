using com.github.fernthedev.lightchat.core.packets;
using System;
using System.Collections.Generic;
using System.Text;

namespace com.github.fernthedev.lightchat.core
{
    public static class StaticHandler
    {

        public static int LineLimit { get; set; } = 8000;
        public static string endLine { get; set; } = "\n\r";

        public static bool Debug { get; set; } = false;
        public static readonly string PACKET_NAMESPACE = typeof(Packet).Namespace;
        public static Encoding encoding { get; set;  } = Encoding.UTF8;

        public static Core core { get; set; }

        public static IJsonHandler defaultJsonHandler { get; set; } = IJsonHandler.enumToHandler(JsonHandlerEnum.Newtonsoft);

    }
}

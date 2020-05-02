using com.github.fernthedev.lightchat.core.packets;
using System;
using System.Collections.Generic;
using System.Text;
using com.github.fernthedev.lightchat.core.data;
using Semver;

namespace com.github.fernthedev.lightchat.core
{
    public static class StaticHandler
    {

        public static int DEFAULT_PACKET_ID_MAX = 10;

        public static int LineLimit { get; set; } = 8000;
        public static string EndLine { get; set; } = "\n\r";

        public static bool Debug { get; set; } = false;
        public static readonly string PACKET_NAMESPACE = typeof(Packet).Namespace;
        public static Encoding encoding { get; set; } = Encoding.UTF8;

        private static Core _core;

        public static Core Core
        {
            get => _core;
            set
            {
                _core = value;
                PacketRegistry.registerDefaultPackets();
            }
        }

        public static IJsonHandler defaultJsonHandler { get; set; } =
            IJsonHandler.enumToHandler(JsonHandlerEnum.DOT_NET_JSON);

        public static readonly VersionData VERSION_DATA = new VersionData("1.6.0", "1.6.0");

        


        public static bool checkVersionRequirements(VersionData otherVer)
        {
            return VERSION_DATA.Version.CompareTo(otherVer.MinVersion) >= 0 &&
                   VERSION_DATA.MinVersion.CompareTo(otherVer.MinVersion) <= 0;
        }


        public static int getVersionRangeStatus(VersionData otherVersion)
        {
            return getVersionRangeStatus(VERSION_DATA, otherVersion);
        }

        public static int getVersionRangeStatus(VersionData versionData, VersionData otherVersion)
        {
            var current = versionData.Version;
            var min = versionData.MinVersion;

            var otherCurrent = otherVersion.Version;
            var otherMin = otherVersion.MinVersion;

            // Current version is smaller than the server's required minimum
            if (current.CompareTo(otherMin) < 0)
            {
                return VersionRange.WE_ARE_LOWER;
            }
            else

                // Current version is larger than server's minimum version
            {
                return min.CompareTo(otherCurrent) > 0 ? VersionRange.WE_ARE_HIGHER : VersionRange.MATCH_REQUIREMENTS;
            }
        }

        public static void displayVersion()
        {
            Core.Logger.Info("Running the version: {} minimum required: {}",
                StaticHandler.VERSION_DATA.Version.ToString(), StaticHandler.VERSION_DATA.MinVersion.ToString());
        }

        public class VersionRange
        {
            public static int OTHER_IS_LOWER = -1,
                WE_ARE_HIGHER = -1,
                MATCH_REQUIREMENTS = 0,
                WE_ARE_LOWER = 1,
                OTHER_IS_HIGHER = 1;
        }
    }
}
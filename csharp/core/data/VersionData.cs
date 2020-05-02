using System;
using System.Diagnostics.CodeAnalysis;
using Newtonsoft.Json;
using Semver;

namespace com.github.fernthedev.lightchat.core.data
{

    public class VersionData
    {

        [NotNull]
        public SemVersion Version { get; }

        [NotNull]
        public SemVersion MinVersion { get; }

        public VersionData(string version, string minVersion)
        {
            Version = SemVersion.Parse(version);
            MinVersion = SemVersion.Parse(minVersion);
        }
    }

    /**
     * JSON friendly version data
     */
    [Serializable]
    public class VersionDataString
    {

        public VersionDataString(VersionData versionData) : this(version: versionData.Version.ToString(), minVersion: versionData.MinVersion.ToString())
        {

        }

        [JsonConstructor]
        public VersionDataString(string version, string minVersion)
        {
            this.version = version ?? throw new ArgumentNullException(nameof(version));
            this.minVersion = minVersion ?? throw new ArgumentNullException(nameof(minVersion));
        }

        public string version { get; }
        public string minVersion { get; }
    }
}
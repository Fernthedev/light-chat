using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Text;

namespace com.github.fernthedev.lightchat.core.encryption
{
    public class EncryptedBytes
    {
        public readonly byte[] data;

        [JsonProperty("params")]
        public readonly byte[] keyParams;
        public readonly string paramAlgorithm;

        public EncryptedBytes(byte[] data, byte[] keyParams, string paramAlgorithm)
        {
            this.data = data;
            this.keyParams = keyParams;
            this.paramAlgorithm = paramAlgorithm;
        }
    }
}

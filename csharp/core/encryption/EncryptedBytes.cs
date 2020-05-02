using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;
using System.Diagnostics.CodeAnalysis;
using System.Linq;
using System.Text;
using System.Text.Json.Serialization;
using com.github.fernthedev.lightchat.core.util;

namespace com.github.fernthedev.lightchat.core.encryption
{
    public class EncryptedBytes
    {
        [NotNull]
        public List<sbyte> data { get; }

        [NotNull]
        [JsonProperty("params", Required = Required.Always)]
        [JsonPropertyName("params")]
        public List<sbyte> keyParams { get;  }

        [NotNull]
        public string paramAlgorithm { get; }

        public EncryptedBytes(IEnumerable<sbyte> data, [JsonProperty(propertyName: "params")] IEnumerable<sbyte> keyParams, string paramAlgorithm)
        {
            this.data = data.ToList();
            this.keyParams = keyParams.ToList();
            this.paramAlgorithm = paramAlgorithm;
        }


        // [JsonConstructor]
        // public EncryptedBytes(List<int> data, [JsonProperty(propertyName: "params")] List<int> keyParams, string paramAlgorithm)
        // {
        //     this.data = data.Select(i => (sbyte) i).ToList();
        //     this.keyParams = keyParams.Select(i => (byte) i).ToList();
        //     this.paramAlgorithm = paramAlgorithm;
        // }
    }
}

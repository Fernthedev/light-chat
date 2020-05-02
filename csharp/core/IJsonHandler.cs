using com.github.fernthedev.lightchat.core.util;
using Newtonsoft.Json;
using System;
using System.Collections.Generic;
using System.Text;
using System.Text.Json;
using JsonSerializer = System.Text.Json.JsonSerializer;

namespace com.github.fernthedev.lightchat.core
{
    public interface IJsonHandler
    {
        string toJson(object o);

        T fromJson<T>(string json);

        T fromJson<T>(string json, Type type);

        public static IJsonHandler enumToHandler(JsonHandlerEnum enumJson)
        {
            switch(enumJson)
            {
                case JsonHandlerEnum.NEWTONSOFT:
                    return new NewtonsoftJsonHandler();
                case JsonHandlerEnum.DOT_NET_JSON:
                    return new DotNetJsonHandler();
            }

            throw new NotSupportedException("The enum provided has not been properly registered in this method");
        } 
    }

    public class NewtonsoftJsonHandler : IJsonHandler
    {
        private static readonly JsonSerializerSettings settings = new JsonSerializerSettings()
        {
            ConstructorHandling = ConstructorHandling.AllowNonPublicDefaultConstructor,
            TypeNameHandling = TypeNameHandling.Auto,

        };

        public T fromJson<T>(string json)
        {
            return JsonConvert.DeserializeObject<T>(json, settings: settings);
        }

        public T fromJson<T>(string json, Type type)
        {
            return (T) JsonConvert.DeserializeObject(json, type, settings: settings);
        }


        public string toJson(object o)
        {
            return JsonConvert.SerializeObject(o);
        }
    }

    public class DotNetJsonHandler : IJsonHandler
    {

        private static readonly JsonSerializerOptions options = new JsonSerializerOptions()
        {
            AllowTrailingCommas = true

        };

        public T fromJson<T>(string json)
        {
            return JsonSerializer.Deserialize<T>(json, options: options);
        }

        public T fromJson<T>(string json, Type type)
        {
            return (T) JsonSerializer.Deserialize(json, type, options: options);
        }

        public string toJson(object o)
        {
            return JsonSerializer.Serialize(o);
        }
    }

    public enum JsonHandlerEnum
    {
        NEWTONSOFT,
        DOT_NET_JSON,
    
    }
    
}

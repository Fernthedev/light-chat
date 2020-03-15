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

        T fromJson<T>(string json, GenericType<T> type)
        {
            return fromJson<T>(json);
        }

        public static IJsonHandler enumToHandler(JsonHandlerEnum enumJson)
        {
            switch(enumJson)
            {
                case JsonHandlerEnum.Newtonsoft:
                    return new NewtonsoftJsonHandler();
                case JsonHandlerEnum.DotNetJson:
                    return new DotNetJsonHandler();
            }

            throw new NotSupportedException("The enum provided has not been properly registered in this method");
        } 
    }

    public class NewtonsoftJsonHandler : IJsonHandler
    {
        public T fromJson<T>(string json)
        {
            return JsonConvert.DeserializeObject<T>(json);
        }



        public string toJson(object o)
        {
            return JsonConvert.SerializeObject(o);
        }
    }

    public class DotNetJsonHandler : IJsonHandler
    {
        public T fromJson<T>(string json)
        {
            return JsonSerializer.Deserialize<T>(json);
        }

        public string toJson(object o)
        {
            return JsonSerializer.Serialize(o);
        }
    }

    public enum JsonHandlerEnum
    {
        Newtonsoft,
        DotNetJson,
    
    }
    
}

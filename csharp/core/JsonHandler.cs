using com.github.fernthedev.lightchat.core.util;
using System;
using System.Collections.Generic;
using System.Text;

namespace com.github.fernthedev.lightchat.core
{
    interface JsonHandler
    {
        string toJson(object o);

        T fromJson<T>(string json, GenericType<T> genericType);
    }
}

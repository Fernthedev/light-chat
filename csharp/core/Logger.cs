using System;
using System.Collections.Generic;
using System.Text;

namespace com.github.fernthedev.lightchat.core
{
    public class Logger
    {
        public void Info(string msg, params string[] objects)
        {
            Console.WriteLine("[Info] " + msg, objects);
        }

        public void Error(string msg, params string[] objects)
        {
            Console.Error.WriteLine("[Error] " + msg, objects);
        }

        public void Warn(string msg, params string[] objects)
        {
            Console.WriteLine("[Warn] " + msg, objects);
        }

        public void Debug(string msg, params string[] objects)
        {
            if (StaticHandler.Debug) Console.WriteLine("[DEBUG] " + msg, objects);
        }
    }
}

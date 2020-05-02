using System;
using System.Collections.Generic;
using System.Text;

namespace com.github.fernthedev.lightchat.core
{
    public abstract class Core
    {
        public Logger Logger { get; } = new Logger();
    }

    public class CoreSettings
    {
        public Encoding Charset { get; set; }  = Encoding.UTF8;
        public long TimeoutMS { get; set;  } = 30 * 1000;

        [NonSerialized]
        public IJsonHandler jsonCodec = IJsonHandler.enumToHandler(DEFAULT_CODEC);

        [NonSerialized]
        protected static readonly JsonHandlerEnum DEFAULT_CODEC = JsonHandlerEnum.NEWTONSOFT;
    }
}

package com.github.fernthedev.lightchat.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.config.Configurator;
import org.reflections.Reflections;
import org.slf4j.Logger;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Log4jDebug {

    public static void setDebug(Logger logger, boolean debug) {
        if (logger != null)
            Configurator.setLevel(logger.getName(), debug ? Level.DEBUG : Level.INFO);


        Configurator.setLevel(Reflections.class.getName(), debug ? Level.DEBUG : Level.WARN);
    }

}

package com.github.fernthedev.client;

import com.github.fernthedev.core.StaticHandler;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;


@RequiredArgsConstructor
public class CLogger implements ILogManager {
    @NonNull
    private Logger logger;

    @Override
    public void log(String log) {
        logger.info(log);
    }

    @Override
    public void logError(String log, Throwable e) {
        logger.error(log,e);
    }

    @Override
    public void info(String s) {
        log(s);
    }

    @Override
    public void debug(String s) {
        if(StaticHandler.isDebug) {
            log("[DEBUG] " + s);
        }
    }

    @Override
    public void error(String s) {
        logger.error(s);
    }
}

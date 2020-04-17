package com.github.fernthedev.lightchat.client;

import com.github.fernthedev.lightchat.core.api.APIUsage;

@APIUsage
@SuppressWarnings("unused")
public interface ILogManager {

    void log(String log);

    void logError(String log, Throwable e);

    void info(String s);

    void debug(String s);

    void error(String s);
}

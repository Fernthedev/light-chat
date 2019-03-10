package com.github.fernthedev.client;

public interface ILogManager {

    public void log(String log);

    public void logError(String log, Throwable e);

    public void info(String s);

    public void debug(String s);
}

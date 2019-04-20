package com.github.fernthedev.server.backend;

import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.event.EventHandler;
import com.github.fernthedev.server.event.Listener;
import com.github.fernthedev.server.event.chat.ChatEvent;
import lombok.Getter;
import okio.BufferedSink;
import okio.Okio;
import okio.Sink;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.impl.MutableLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Plugin(
        name = "LogFileAppender",
        category = Core.CATEGORY_NAME,
        elementType = Appender.ELEMENT_TYPE,
        printObject = true)
@Deprecated
public class LogFileManager extends AbstractAppender implements Listener {

    private static String date;
    private static File logFolder;
    private static File logFile;

    @Getter
    private static LogFileManager instance;
    // for storing the log events
    private List<LogEvent> events = new ArrayList<>();

    protected LogFileManager(
            String name,
            Filter filter,
            Layout<? extends Serializable> layout,
            boolean ignoreExceptions) {
        super(name, filter, layout, ignoreExceptions);
        instance = this;
        construct();
    }

    public List<LogEvent> getEvents() {
        return events;
    }

    @PluginFactory
    public static LogFileManager createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") Filter filter) {
        if (name == null) {
            LOGGER.error("No name provided for TestLoggerAppender");
            return null;
        }

        if (layout == null) layout = PatternLayout.createDefaultLayout();

        return new LogFileManager(name, filter, layout, true);
    }

    @Override
    public void append(LogEvent event) {
        if (event instanceof MutableLogEvent) {
            events.add(((MutableLogEvent) event).createMemento());
        } else {
            events.add(event);
        }
        log(event.getMessage().getFormattedMessage());

    }


    private static void construct() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-yyyy-dd-hh:mm-aa");

        date = simpleDateFormat.format(new Date());

        createFiles();

        try {
            // pipes output in the output stream to the input stream
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(in);



// printing entries in piped input stream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

            /*
            new Thread(() -> {
                String line;
                while (Server.getInstance().isRunning()) {
                    try {
                        while (bufferedReader.ready() && (line = bufferedReader.readLine()) != null) {
                            log(line);
                        }
                    } catch (IOException e){
                        Server.getLogger().error(e.getMessage(),e.getCause());
                    }
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();*/

        } catch (IOException e) {
            Server.getLogger().error(e.getMessage(),e.getCause());
        }
    }

    private static void createFiles() {
        logFolder = new File(System.getProperty("user.dir"),"logs");

        if(!logFolder.exists())
            logFolder.mkdir();

        Server.getLogger().info(date+".log is the file for logging");

        logFile = new File(logFolder, date+".log");
        if(!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                Server.getLogger().error(e.getMessage(),e.getCause());
            }
        }
    }

    @EventHandler
    public void onChatEvent(ChatEvent e) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-yyyy-dd-hh:mm-aa");

        String time = simpleDateFormat.format(new Date());

        String messageToWrite;
        if(e.isCancelled()) messageToWrite =  "[" + time + "] [Cancelled] [" + e.getSender().getName() + "] " + e.getMessage();
            else
         messageToWrite =  "[" + time + "] [" + e.getSender().getName() + "] " + e.getMessage();

        log(messageToWrite);
    }

    public static void log(String message) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-yyyy-dd-hh:mm-aa");

        String time = simpleDateFormat.format(new Date());

        if(logFile == null || !logFile.exists()) {
            createFiles();
        }

        try(Sink fileSink = Okio.sink(logFile);
            BufferedSink writer = Okio.buffer(fileSink)) {

            try {
                writer
                        .writeUtf8(message)
                        .writeUtf8(System.getProperty("line.separator"));
            } catch (IOException e1) {
                Server.getLogger().error(e1.getMessage(), e1.getCause());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

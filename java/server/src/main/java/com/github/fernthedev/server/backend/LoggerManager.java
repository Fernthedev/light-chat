package com.github.fernthedev.server.backend;

import com.github.fernthedev.server.Server;
import com.github.fernthedev.server.event.EventHandler;
import com.github.fernthedev.server.event.Listener;
import com.github.fernthedev.server.event.chat.ChatEvent;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LoggerManager implements Listener {

    private String date;
    private static File logFolder;
    private File logFile;

    private static LoggerManager loggerManager;

    public LoggerManager() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-yyyy-dd-hh:mm-aa");

        date = simpleDateFormat.format(new Date());

        createFiles();
        LoggerManager.loggerManager = this;

        try {
            // pipes output in the output stream to the input stream
            PipedInputStream in = new PipedInputStream();
            PipedOutputStream out = new PipedOutputStream(in);



// printing entries in piped input stream
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in));

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
            }).start();

        } catch (IOException e) {
            Server.getLogger().error(e.getMessage(),e.getCause());
        }
    }

    private void createFiles() {
        logFolder = new File(System.getProperty("user.dir"),"logs");

        if(!logFolder.exists())
            logFolder.mkdir();

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

    public static LoggerManager getInstance() {
        return loggerManager;
    }

    public void log(String message) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM-yyyy-dd-hh:mm-aa");

        String time = simpleDateFormat.format(new Date());

        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(logFile,true));
            if(writer == null) {
                createFiles();
                log(message);
            }
        } catch (IOException e) {
            Server.getLogger().error(e.getMessage(),e.getCause());
        }

        if(writer != null) {
            try {
                writer.write(message + System.getProperty("line.separator"));
                writer.close();
            } catch (IOException e1) {
                Server.getLogger().error(e1.getMessage(),e1.getCause());
            }
        }

    }

}

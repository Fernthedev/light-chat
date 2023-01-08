package com.github.fernthedev.terminal.core;

import com.github.fernthedev.lightchat.core.StaticHandler;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsoleHandler extends SimpleTerminalConsole {

    @Setter
    @NonNull
    private TermCore termCore;

    public static void startConsoleHandlerAsync( TermCore termCore, Completer completeHandler) {
        new Thread(() -> {
            StaticHandler.getCore().getLogger().info("Starting console handler");
            new ConsoleHandler(termCore, completeHandler).start();
            StaticHandler.getCore().getLogger().info("Started console handler");
        },"ConsoleHandler").start();


    }

    @NonNull
    private final Completer completer;

    /**
     * Determines if the application is still running and accepting input.
     *
     * @return {@code true} to continue reading input
     */
    @Override
    protected boolean isRunning() {
        return StaticHandler.getCore().isRunning();
    }

    /**
     * Run a command entered in the console.
     *
     * @param command The command line to run
     */
    @Override
    protected void runCommand(String command) {
        termCore.runCommand(command);
    }

    /**
     * Configures the {@link LineReaderBuilder} and {@link LineReader} with
     * additional options.
     *
     * <p>Override this method to make further changes, (e.g. call
     * {@link LineReaderBuilder#appName(String)} or
     * {@link LineReaderBuilder#completer(Completer)}).</p>
     *
     * <p>The default implementation sets some opinionated default options,
     * which are considered to be appropriate for most applications:</p>
     *
     * <ul>
     *     <li>{@link LineReader.Option#DISABLE_EVENT_EXPANSION}: JLine implements
     *     <a href="http://www.gnu.org/software/bash/manual/html_node/Event-Designators.html">
     *     Bash's Event Designators</a> by default. These usually do not
     *     behave as expected in a simple command environment, so it's
     *     recommended to disable it.</li>
     *     <li>{@link LineReader.Option#INSERT_TAB}: By default, JLine inserts
     *     a tab character when attempting to tab-complete on empty input.
     *     It is more intuitive to show a list of commands instead.</li>
     * </ul>
     *
     * @param builder The builder to configure
     * @return The built line reader
     */
    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        return super.buildReader(builder
                .appName(StaticHandler.getCore().getName())
                .completer(completer)
        );
    }

    /**
     * Shutdown the application and perform a clean exit.
     *
     * <p>This is called if the application receives SIGINT while reading input,
     * e.g. when pressing CTRL+C on most terminal implementations.</p>
     */
    @Override
    protected void shutdown() {
        StaticHandler.getCore().shutdown();
        System.exit(0);
    }
}
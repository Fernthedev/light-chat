package com.github.fernthedev.terminal.core

import com.github.fernthedev.lightchat.core.StaticHandler.core
import net.minecrell.terminalconsole.SimpleTerminalConsole
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder

class ConsoleHandler private constructor(private var termCore: TermCore, private val completer: Completer) :
    SimpleTerminalConsole() {
    /**
     * Determines if the application is still running and accepting input.
     *
     * @return `true` to continue reading input
     */
    override fun isRunning(): Boolean {
        return core.isRunning
    }

    /**
     * Run a command entered in the console.
     *
     * @param command The command line to run
     */
    override fun runCommand(command: String) {
        termCore.runCommand(command)
    }

    /**
     * Configures the [LineReaderBuilder] and [LineReader] with
     * additional options.
     *
     *
     * Override this method to make further changes, (e.g. call
     * [LineReaderBuilder.appName] or
     * [LineReaderBuilder.completer]).
     *
     *
     * The default implementation sets some opinionated default options,
     * which are considered to be appropriate for most applications:
     *
     *
     *  * [LineReader.Option.DISABLE_EVENT_EXPANSION]: JLine implements
     * [
 * Bash's Event Designators](http://www.gnu.org/software/bash/manual/html_node/Event-Designators.html) by default. These usually do not
     * behave as expected in a simple command environment, so it's
     * recommended to disable it.
     *  * [LineReader.Option.INSERT_TAB]: By default, JLine inserts
     * a tab character when attempting to tab-complete on empty input.
     * It is more intuitive to show a list of commands instead.
     *
     *
     * @param builder The builder to configure
     * @return The built line reader
     */
    override fun buildReader(builder: LineReaderBuilder): LineReader {
        return super.buildReader(
            builder
                .appName(core.name)
                .completer(completer)
        )
    }

    /**
     * Shutdown the application and perform a clean exit.
     *
     *
     * This is called if the application receives SIGINT while reading input,
     * e.g. when pressing CTRL+C on most terminal implementations.
     */
    override fun shutdown() {
        core.shutdown()
        System.exit(0)
    }

    fun setTermCore(termCore: TermCore) {
        this.termCore = termCore
    }

    companion object {
        @JvmStatic
        fun startConsoleHandlerAsync(termCore: TermCore, completeHandler: Completer) {
            Thread({
                core.logger.info("Starting console handler")
                ConsoleHandler(termCore, completeHandler).start()
                core.logger.info("Started console handler")
            }, "ConsoleHandler").start()
        }
    }
}
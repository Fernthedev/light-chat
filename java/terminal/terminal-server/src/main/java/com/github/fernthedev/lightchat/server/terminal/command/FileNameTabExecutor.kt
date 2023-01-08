package com.github.fernthedev.lightchat.server.terminal.command

import com.github.fernthedev.lightchat.server.SenderInterface
import org.jline.builtins.Completers
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.function.Consumer

class FileNameTabExecutor : Completers.FileNameCompleter(), TabExecutor {
    /**
     * TabExecutor implementation of [org.jline.builtins.Completers.FileNameCompleter]
     *
     * A file name completer takes the buffer and issues a list of
     * potential completions.
     *
     *
     * This completer tries to behave as similar as possible to
     * *bash*'s file name completion (using GNU readline)
     * with the following exceptions:
     *
     *  * Candidates that are directories will end with "/"
     *  * Wildcard regular expressions are not evaluated or replaced
     *  * The "~" character can be used to represent the user's home,
     * but it cannot complete to other users' homes, since java does
     * not provide any way of determining that easily
     *
     *
     * @see org.jline.builtins.Completers.FileNameCompleter
     *
     * @since 2.3
     */
    override fun getCompletions(sender: SenderInterface, args: Deque<String>): List<String> {
        val candidates: MutableList<String> = ArrayList()
        val commandLine = args.last!!
        val current: Path
        val curBuf: String
        val sep = getSeparator(true)
        val lastSep = commandLine.lastIndexOf(sep)

        try {
            if (lastSep >= 0) {
                curBuf = commandLine.substring(0, lastSep + 1)
                current = if (curBuf.startsWith("~")) {
                    if (curBuf.startsWith("~$sep")) {
                        userHome.resolve(curBuf.substring(2))
                    } else {
                        userHome.parent.resolve(curBuf.substring(1))
                    }
                } else {
                    userDir.resolve(curBuf)
                }
            } else {
                curBuf = ""
                current = userDir
            }
            try {
                Files.newDirectoryStream(current) { path: Path? -> accept(path) }
                    .use { directory ->
                        directory.forEach(
                            Consumer { p: Path ->
                                val value = curBuf + p.fileName.toString()
                                if (Files.isDirectory(p)) {
                                    candidates.add(value + sep)
                                } else {
                                    candidates.add(value)
                                }
                            })
                    }
            } catch (e: IOException) {
                // Ignore
            }
        } catch (e: Exception) {
            // Ignore
        }
        return candidates
    }
}
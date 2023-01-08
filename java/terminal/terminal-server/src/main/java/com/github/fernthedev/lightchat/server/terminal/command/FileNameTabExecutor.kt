package com.github.fernthedev.lightchat.server.terminal.command;

import com.github.fernthedev.lightchat.server.SenderInterface;
import org.jline.builtins.Completers;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FileNameTabExecutor extends Completers.FileNameCompleter implements TabExecutor {

    /**
     * TabExecutor implementation of {@link org.jline.builtins.Completers.FileNameCompleter}
     *
     * A file name completer takes the buffer and issues a list of
     * potential completions.
     * <p>
     * This completer tries to behave as similar as possible to
     * <i>bash</i>'s file name completion (using GNU readline)
     * with the following exceptions:
     * <ul>
     * <li>Candidates that are directories will end with "/"</li>
     * <li>Wildcard regular expressions are not evaluated or replaced</li>
     * <li>The "~" character can be used to represent the user's home,
     * but it cannot complete to other users' homes, since java does
     * not provide any way of determining that easily</li>
     * </ul>
     *
     * @see org.jline.builtins.Completers.FileNameCompleter
     * @since 2.3
     */
    @Override
    public List<String> getCompletions(SenderInterface sender, LinkedList<String> args) {

        List<String> candidates = new ArrayList<>();
        String commandLine = args.getLast();

        assert commandLine != null;

        Path current;
        String curBuf;
        String sep = getSeparator(true);
        int lastSep = commandLine.lastIndexOf(sep);
        try {
            if (lastSep >= 0) {
                curBuf = commandLine.substring(0, lastSep + 1);
                if (curBuf.startsWith("~")) {
                    if (curBuf.startsWith("~" + sep)) {
                        current = getUserHome().resolve(curBuf.substring(2));
                    } else {
                        current = getUserHome().getParent().resolve(curBuf.substring(1));
                    }
                } else {
                    current = getUserDir().resolve(curBuf);
                }
            } else {
                curBuf = "";
                current = getUserDir();
            }
            try (DirectoryStream<Path> directory = Files.newDirectoryStream(current, this::accept)) {
                directory.forEach(p -> {
                    String value = curBuf + p.getFileName().toString();
                    if (Files.isDirectory(p)) {
                        candidates.add(value + sep);
                    } else {
                        candidates.add(value);
                    }
                });
            } catch (IOException e) {
                // Ignore
            }
        } catch (Exception e) {
            // Ignore
        }

        return candidates;
    }
}

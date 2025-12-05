package com.aicode.reviewer.diff;

import com.aicode.reviewer.enums.LineChangeType;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class DiffParser {

    private static final Pattern HUNK_HEADER = Pattern.compile("@@ -(\\d+)(?:,(\\d+))? \+(\\d+)(?:,(\\d+))? @@.*");

    public List<DiffFile> parse(String diffContent) {
        List<DiffFile> files = new ArrayList<>();
        if (!StringUtils.hasText(diffContent)) {
            return files;
        }

        String[] lines = diffContent.split("\\r?\\n");
        DiffFile currentFile = null;
        DiffHunk currentHunk = null;
        int oldLine = 0;
        int newLine = 0;

        for (String line : lines) {
            if (line.startsWith("diff --git")) {
                currentFile = new DiffFile();
                files.add(currentFile);
                currentHunk = null;
                continue;
            }

            if (currentFile == null) {
                continue;
            }

            if (line.startsWith("index ")) {
                continue;
            }

            if (line.startsWith("--- ")) {
                currentFile.setOldFile(stripPrefix(line.substring(4)));
                continue;
            }

            if (line.startsWith("+++ ")) {
                currentFile.setNewFile(stripPrefix(line.substring(4)));
                continue;
            }

            if (line.startsWith("Binary files")) {
                currentFile.setBinary(true);
                continue;
            }

            if (line.startsWith("@@")) {
                Matcher matcher = HUNK_HEADER.matcher(line);
                if (matcher.matches()) {
                    currentHunk = new DiffHunk();
                    currentHunk.setHeader(line);
                    currentHunk.setOldStart(Integer.parseInt(matcher.group(1)));
                    currentHunk.setOldLines(groupOrDefault(matcher.group(2)));
                    currentHunk.setNewStart(Integer.parseInt(matcher.group(3)));
                    currentHunk.setNewLines(groupOrDefault(matcher.group(4)));
                    oldLine = currentHunk.getOldStart();
                    newLine = currentHunk.getNewStart();
                    currentFile.getHunks().add(currentHunk);
                } else {
                    log.warn("Unable to parse hunk header: {}", line);
                }
                continue;
            }

            if (currentHunk == null) {
                continue;
            }

            if (line.startsWith("+")) {
                currentHunk.getChanges().add(LineChange.builder()
                        .type(LineChangeType.ADDED)
                        .content(line.substring(1))
                        .oldLineNumber(oldLine - 1)
                        .newLineNumber(newLine)
                        .build());
                newLine++;
            } else if (line.startsWith("-")) {
                currentHunk.getChanges().add(LineChange.builder()
                        .type(LineChangeType.REMOVED)
                        .content(line.substring(1))
                        .oldLineNumber(oldLine)
                        .newLineNumber(newLine - 1)
                        .build());
                oldLine++;
            } else {
                currentHunk.getChanges().add(LineChange.builder()
                        .type(LineChangeType.CONTEXT)
                        .content(line.startsWith(" ") ? line.substring(1) : line)
                        .oldLineNumber(oldLine)
                        .newLineNumber(newLine)
                        .build());
                oldLine++;
                newLine++;
            }
        }

        return files;
    }

    private int groupOrDefault(String value) {
        return value == null ? 0 : Integer.parseInt(value);
    }

    private String stripPrefix(String path) {
        if (!StringUtils.hasText(path)) {
            return path;
        }
        if (path.startsWith("a/") || path.startsWith("b/")) {
            return path.substring(2);
        }
        return path;
    }
}

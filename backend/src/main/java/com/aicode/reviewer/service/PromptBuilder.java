package com.aicode.reviewer.service;

import com.aicode.reviewer.diff.DiffFile;
import com.aicode.reviewer.diff.DiffHunk;
import com.aicode.reviewer.diff.LineChange;
import com.aicode.reviewer.enums.LineChangeType;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class PromptBuilder {

    public String buildReviewPrompt(String repo, int prNumber, List<DiffFile> files) {
        return "You are an elite software reviewer. Analyze pull request " + repo + "#" + prNumber +
                " and provide:\n" +
                "1. A concise summary of the user impact.\n" +
                "2. Concrete findings (bug, risk, refactor suggestions).\n" +
                "3. Ref test coverage, note missing tests.\n" +
                "Respond as JSON with keys summary, findings[].category/severity/message/suggestion/file/line, missingTests (array).\n\n" +
                diffExcerpt(files);
    }

    public String buildTestPrompt(String repo, List<DiffFile> files) {
        return "Generate missing automated tests for repo " + repo + ". Provide JSON array with file, framework, scenario, examplePseudo.\n\n"
                + diffExcerpt(files);
    }

    private String diffExcerpt(List<DiffFile> files) {
        return files.stream()
                .limit(8)
                .map(file -> "File: " + file.displayName() + "\n" + summarize(file.getHunks()))
                .collect(Collectors.joining("\n\n"));
    }

    private String summarize(List<DiffHunk> hunks) {
        return hunks.stream()
                .limit(3)
                .map(hunk -> {
                    long additions = hunk.getChanges().stream().filter(c -> c.getType() == LineChangeType.ADDED).count();
                    long removals = hunk.getChanges().stream().filter(c -> c.getType() == LineChangeType.REMOVED).count();
                    LineChange firstChange = hunk.getChanges().stream().findFirst().orElse(null);
                    String sample = firstChange != null ? firstChange.getContent() : "";
                    return String.format("@@ %s ( +%d -%d ) example: %s", hunk.getHeader(), additions, removals, sample);
                })
                .collect(Collectors.joining("\n"));
    }
}

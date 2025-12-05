package com.aicode.reviewer.diff;

import static org.assertj.core.api.Assertions.assertThat;

import com.aicode.reviewer.enums.LineChangeType;
import java.util.List;
import org.junit.jupiter.api.Test;

class DiffParserTest {

    private final DiffParser parser = new DiffParser();

    @Test
    void parsesUnifiedDiff() {
        String diff = "diff --git a/App.java b/App.java\n"
                + "index 83db... 0123\n"
                + "--- a/App.java\n"
                + "+++ b/App.java\n"
                + "@@ -1,3 +1,4 @@\n"
                + " import java.util.*;\n"
                + "+import java.time.Instant;\n"
                + " public class App {\n"
                + "-    // TODO\n"
                + "+    // done\n"
                + " }\n";

        List<DiffFile> files = parser.parse(diff);
        assertThat(files).hasSize(1);
        DiffFile file = files.get(0);
        assertThat(file.getHunks()).hasSize(1);
        DiffHunk hunk = file.getHunks().get(0);
        long additions = hunk.getChanges().stream().filter(c -> c.getType() == LineChangeType.ADDED).count();
        long removals = hunk.getChanges().stream().filter(c -> c.getType() == LineChangeType.REMOVED).count();
        assertThat(additions).isEqualTo(2);
        assertThat(removals).isEqualTo(1);
        assertThat(file.displayName()).isEqualTo("App.java");
    }
}

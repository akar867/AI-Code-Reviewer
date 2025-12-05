package com.aicode.reviewer.diff;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class DiffFile {
    private String oldFile;
    private String newFile;
    private boolean binary;
    private final List<DiffHunk> hunks = new ArrayList<>();

    public String displayName() {
        return newFile != null ? newFile : oldFile;
    }
}

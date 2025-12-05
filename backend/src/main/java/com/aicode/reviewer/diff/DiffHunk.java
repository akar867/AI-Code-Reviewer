package com.aicode.reviewer.diff;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class DiffHunk {
    private String header;
    private int oldStart;
    private int oldLines;
    private int newStart;
    private int newLines;
    private final List<LineChange> changes = new ArrayList<>();
}

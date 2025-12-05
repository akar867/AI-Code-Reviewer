package com.aicode.reviewer.diff;

import com.aicode.reviewer.enums.LineChangeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class LineChange {
    private LineChangeType type;
    private int oldLineNumber;
    private int newLineNumber;
    private String content;
}

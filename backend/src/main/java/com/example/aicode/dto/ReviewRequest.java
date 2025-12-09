package com.example.aicode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReviewRequest {

    @NotBlank(message = "Code snippet is required")
    @Size(max = 20000, message = "Code snippet is too large")
    private String code;

    @NotBlank(message = "Language selection is required")
    private String ref;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }
}

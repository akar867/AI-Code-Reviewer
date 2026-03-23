package com.example.aicode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ReviewRequest {

	@NotBlank(message = "Code snippet is required")
	@Size(max = 20000, message = "Code snippet is too large")
	private String code;

	@NotBlank(message = "Language selection is required")
	private String language;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getlanguage() {
		return language;
	}

	public void setlanguage(String language) {
		this.language = language;
	}

	
}

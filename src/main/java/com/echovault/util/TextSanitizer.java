package com.echovault.util;

import java.util.regex.Pattern;

public class TextSanitizer {

    // Regex to match [span_X](start_span), [span_X](end_span), and standalone [span_X]
    private static final Pattern SPAN_TAG_PATTERN = Pattern.compile("\\[span_\\d+\\]\\((?:start|end)_span\\)|\\[span_\\d+\\]");
    private static final Pattern MULTIPLE_SPACES_PATTERN = Pattern.compile("\\s+");

    public static String cleanAiResponse(String rawText) {
        if (rawText == null || rawText.isBlank()) {
            return "";
        }

        // Strip span annotations
        String cleaned = SPAN_TAG_PATTERN.matcher(rawText).replaceAll("");
        
        // Collapse multi-spaces created by tag removal and trim outer whitespaces
        return MULTIPLE_SPACES_PATTERN.matcher(cleaned).replaceAll(" ").trim();
    }
}

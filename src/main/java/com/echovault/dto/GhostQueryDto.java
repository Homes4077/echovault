package com.echovault.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GhostQueryDto {
    private String prompt;
    private String query;

    public String getEffectivePrompt() {
        if (prompt != null && !prompt.trim().isEmpty()) {
            return prompt;
        }
        return query;
    }
}

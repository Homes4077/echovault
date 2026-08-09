package com.echovault.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VoiceUploadDto {
    private Long userId;
    private String title;
    private String tag;
}

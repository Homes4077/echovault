package com.echovault.dto;

import com.echovault.model.VoiceNote;
import lombok.Data;

@Data
public class VoiceUploadDto {
    private Long userId;
    private String title;
    private VoiceNote.Tag tag;
}

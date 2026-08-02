package com.echovault.dto;

import com.echovault.model.VoiceNote;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class VoiceUploadDto {
    private Long userId;
    private String title;
    private VoiceNote.Tag tag;
    private MultipartFile file;
}

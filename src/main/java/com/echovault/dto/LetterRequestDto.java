package com.echovault.dto;

import com.echovault.model.VoiceNote;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class LetterRequestDto {
    private Long userId;
    private String recipientName;
    private String recipientEmail;
    private String subject;
    private String bodyContent;
    private VoiceNote.Tag tag;
    private LocalDateTime scheduledDeliveryAt;
}

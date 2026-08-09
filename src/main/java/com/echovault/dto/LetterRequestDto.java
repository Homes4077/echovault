package com.echovault.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LetterRequestDto {
    private Long userId;
    private String recipientName;
    private String recipientEmail;
    private String subject;
    private String bodyContent;
    private String tag;
    private LocalDateTime scheduledDeliveryAt;
}

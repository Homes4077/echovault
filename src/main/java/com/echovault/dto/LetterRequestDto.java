package com.echovault.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LetterRequestDto {

    private String recipientName;
    private String recipientEmail;
    private String subject;
    private String bodyContent;
    private String tag;
    private LocalDateTime scheduledDeliveryAt;
}

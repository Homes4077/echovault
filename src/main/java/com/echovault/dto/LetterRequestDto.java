package com.echovault.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduledDeliveryAt;
}

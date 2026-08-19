package com.echovault.dto; // Fixed lowercase 'package' keyword

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    private String title;
    private String subject;
    private String content;
    private String bodyContent;
    private String tag;

    // Flexible pattern to parse ISO strings with or without seconds
    @JsonFormat(pattern = "yyyy-MM-dd['T'][' '][HH:mm:ss][HH:mm]")
    private LocalDateTime scheduledDeliveryAt;

    @JsonProperty("isPublic") // Ensures Jackson maps "isPublic" from JSON body
    private boolean isPublic;
}

package com.echovault.dto;

import lombok.Data;

@Data
public class SecurityUnlockDto {
    private String email;
    private String securityAnswer;
}

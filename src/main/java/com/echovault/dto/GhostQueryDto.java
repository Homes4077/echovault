package com.echovault.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GhostQueryDto {
    private Long vaultOwnerId;
    private Long queriedById;
    private String queryText;
}

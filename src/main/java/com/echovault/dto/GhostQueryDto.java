package com.echovault.dto;

import lombok.Data;

@Data
public class GhostQueryDto {
    private Long vaultOwnerId;
    private Long queriedById;
    private String queryText;
    private String responseText;
    private String sourcesUsed;
}

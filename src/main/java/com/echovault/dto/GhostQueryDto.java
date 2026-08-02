lpackage com.echovault.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GhostQueryDto {

    private Long vaultOwnerId;
    private String queryText;
    private String responseText;
    private String sourcesUsed;
}

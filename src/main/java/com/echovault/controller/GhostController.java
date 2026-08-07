package com.echovault.controller;

import com.echovault.dto.GhostQueryDto;
import com.echovault.model.GhostQuery;
import com.echovault.model.User;
import com.echovault.repository.GhostQueryRepository;
import com.echovault.repository.UserRepository;
import com.echovault.service.GhostEngineService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ghost")
public class GhostController {

    private final GhostQueryRepository ghostQueryRepository;
    private final UserRepository userRepository;
    private final GhostEngineService ghostEngineService;

    public GhostController(GhostQueryRepository ghostQueryRepository,
                           UserRepository userRepository,
                           GhostEngineService ghostEngineService) {
        this.ghostQueryRepository = ghostQueryRepository;
        this.userRepository = userRepository;
        this.ghostEngineService = ghostEngineService;
    }

    @PostMapping("/ask")
    public ResponseEntity<?> askGhostEngine(@RequestBody GhostQueryDto dto) {
        try {
            String aiResponse = ghostEngineService.generateResponse(dto.getVaultOwnerId(), dto.getQueryText());
            return ResponseEntity.ok(Map.of(
                "vaultOwnerId", dto.getVaultOwnerId(),
                "query", dto.getQueryText(),
                "response", aiResponse
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error generating response: " + e.getMessage());
        }
    }

    @PostMapping("/log")
    public ResponseEntity<?> logQuery(@RequestBody GhostQueryDto dto) {
        User owner = userRepository.findById(dto.getVaultOwnerId()).orElse(null);
        User queriedBy = userRepository.findById(dto.getQueriedById()).orElse(null);

        if (owner == null || queriedBy == null) {
            return ResponseEntity.badRequest().body("Invalid vault owner or querying user ID");
        }

        GhostQuery query = GhostQuery.builder()
            .vaultOwner(owner)
            .queriedBy(queriedBy)
            .queryText(dto.getQueryText())
            .build();

        return ResponseEntity.ok(ghostQueryRepository.save(query));
    }

    @GetMapping("/history/{ownerId}")
    public ResponseEntity<List<GhostQuery>> getHistory(@PathVariable Long ownerId) {
        return ResponseEntity.ok(ghostQueryRepository.findByVaultOwnerId(ownerId));
    }
}

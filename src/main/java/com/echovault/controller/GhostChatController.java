package com.echovault.controller;

import com.echovault.dto.GhostQueryDto;
import com.echovault.service.GhostEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/ghost")
@RequiredArgsConstructor
public class GhostChatController {

    private final GhostEngineService ghostEngineService;

    @PostMapping("/chat")
    public ResponseEntity<?> chat(@RequestBody GhostQueryDto queryDto, Principal principal) {
        String prompt = queryDto.getEffectivePrompt();
        if (prompt == null || prompt.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt or query is required."));
        }

        String userEmail = (principal != null) ? principal.getName() : "anonymous";
        String aiResponse = ghostEngineService.processQuery(userEmail, prompt);

        return ResponseEntity.ok(Map.of("response", aiResponse));
    }
}

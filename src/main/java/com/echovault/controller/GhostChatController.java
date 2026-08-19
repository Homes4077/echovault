package com.echovault.controller;

import com.echovault.dto.GhostQueryDto;
import com.echovault.service.GhostEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class GhostChatController {

    private final GhostEngineService ghostEngineService;

    @PostMapping({"/api/ghost/chat", "/api/ghost-chat"})
    public ResponseEntity<?> chat(@RequestBody(required = false) GhostQueryDto queryDto, 
                                  @RequestBody(required = false) Map<String, String> bodyMap, 
                                  Principal principal) {
        
        String prompt = null;

        if (queryDto != null && queryDto.getEffectivePrompt() != null) {
            prompt = queryDto.getEffectivePrompt();
        } else if (bodyMap != null) {
            prompt = bodyMap.getOrDefault("prompt", bodyMap.get("message"));
        }

        if (prompt == null || prompt.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Prompt or message is required."));
        }

        String userEmail = (principal != null) ? principal.getName() : "liljaymar254@gmail.com";
        String aiResponse = ghostEngineService.processQuery(userEmail, prompt);

        // Provides compatibility for both data.response and data.reply in JS clients
        return ResponseEntity.ok(Map.of(
            "response", aiResponse,
            "reply", aiResponse,
            "message", aiResponse
        ));
    }
}

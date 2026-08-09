package com.echovault.controller;

import com.echovault.service.GhostEngineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ghost")
@RequiredArgsConstructor
public class GhostController {

    private final GhostEngineService ghostEngineService;

    @PostMapping("/interrogate")
    public ResponseEntity<String> interrogate(@RequestParam String query, @RequestParam String userIdentifier) {
        String response = ghostEngineService.interrogateVault(query, userIdentifier);
        return ResponseEntity.ok(response);
    }
}

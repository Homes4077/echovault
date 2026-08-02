package com.echovault.controller;

import com.echovault.dto.GhostQueryDto;
import com.echovault.model.*;
import com.echovault.repository.*;
import com.echovault.service.GeminiGhostEngineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/ghost")
public class GhostEngineController {

    @Autowired private UserRepository userRepository;
    @Autowired private VoiceNoteRepository voiceNoteRepository;
    @Autowired private LetterRepository letterRepository;
    @Autowired private GhostQueryRepository ghostQueryRepository;
    @Autowired private GeminiGhostEngineService ghostEngineService;

    @PostMapping("/query")
    @ResponseBody
    public GhostQueryDto processGhostQuery(@RequestBody GhostQueryDto dto) {
        User vaultOwner = userRepository.findById(dto.getVaultOwnerId()).orElseThrow();
        User queriedBy = userRepository.findById(dto.getQueriedById()).orElseThrow();

        // Retrieve ground truth memories from voice notes and letters
        List<String> contentEntries = new ArrayList<>();
        List<String> sourceIds = new ArrayList<>();

        List<VoiceNote> voiceNotes = voiceNoteRepository.findByUserId(vaultOwner.getId());
        for (VoiceNote vn : voiceNotes) {
            if (vn.getTranscription() != null) {
                contentEntries.add("Voice Note (" + vn.getTag() + "): " + vn.getTranscription());
                sourceIds.add("VN-" + vn.getId());
            }
        }

        List<Letter> letters = letterRepository.findByUserId(vaultOwner.getId());
        for (Letter l : letters) {
            contentEntries.add("Letter (" + l.getTag() + "): " + l.getBodyContent());
            sourceIds.add("LT-" + l.getId());
        }

        // Query Gemini Ghost Engine with strict zero-fabrication prompt
        String response = ghostEngineService.queryGhostEngine(
            vaultOwner.getFullName(),
            contentEntries,
            dto.getQueryText()
        );

        // Audit query in DB
        GhostQuery queryLog = new GhostQuery();
        queryLog.setVaultOwner(vaultOwner);
        queryLog.setQueriedBy(queriedBy);
        queryLog.setQueryText(dto.getQueryText());
        queryLog.setResponseText(response);
        queryLog.setSourcesUsed(sourceIds.toString());
        ghostQueryRepository.save(queryLog);

        dto.setResponseText(response);
        dto.setSourcesUsed(sourceIds.toString());
        return dto;
    }
}

package com.echovault.service;

import com.echovault.model.Letter;
import com.echovault.model.VoiceNote;
import com.echovault.repository.LetterRepository;
import com.echovault.repository.VoiceNoteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GeminiGhostEngineService implements GhostEngineService {

    private final LetterRepository letterRepository;
    private final VoiceNoteRepository voiceNoteRepository;

    public GeminiGhostEngineService(LetterRepository letterRepository, VoiceNoteRepository voiceNoteRepository) {
        this.letterRepository = letterRepository;
        this.voiceNoteRepository = voiceNoteRepository;
    }

    @Override
    public String generateResponse(Long vaultOwnerId, String queryText) {
        List<Letter> letters = letterRepository.findByUserId(vaultOwnerId);
        List<VoiceNote> voiceNotes = voiceNoteRepository.findByUserId(vaultOwnerId);

        String letterContext = letters.stream()
            .map(Letter::getBodyContent)
            .collect(Collectors.joining("\n"));

        String voiceContext = voiceNotes.stream()
            .filter(v -> v.getTranscription() != null)
            .map(VoiceNote::getTranscription)
            .collect(Collectors.joining("\n"));

        String fullContext = letterContext + "\n" + voiceContext;

        if (fullContext.isBlank()) {
            return "No vault memory context found to generate a response.";
        }

        return "Gemini Response based on context: " + (fullContext.length() > 100 ? fullContext.substring(0, 100) + "..." : fullContext);
    }
}

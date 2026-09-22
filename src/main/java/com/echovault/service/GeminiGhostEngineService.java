package com.echovault.service;

import com.echovault.model.Letter;
import com.echovault.model.User;
import com.echovault.model.VoiceNote;
import com.echovault.repository.LetterRepository;
import com.echovault.repository.UserRepository;
import com.echovault.repository.VoiceNoteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

/**
 * AI Ghost Companion Service for EchoVault.
 * Combines written letters and recorded voice notes with the Gemini model
 * to act as a warm, intuitive alter-ego and digital legacy companion.
 */
@Slf4j
@Service
public class GeminiGhostEngineService {

    private final LetterRepository letterRepository;
    private final UserRepository userRepository;
    private final VoiceNoteRepository voiceNoteRepository;
    private final RestTemplate restTemplate;

    @Value("${gemini.api.key:${gcp.api.key:}}")
    private String apiKey;

    @Value("${gemini.api.model:gemini-1.5-flash}")
    private String modelName;

    @Autowired
    public GeminiGhostEngineService(
            LetterRepository letterRepository,
            UserRepository userRepository,
            @Autowired(required = false) VoiceNoteRepository voiceNoteRepository,
            RestTemplateBuilder restTemplateBuilder) {
        this.letterRepository = letterRepository;
        this.userRepository = userRepository;
        this.voiceNoteRepository = voiceNoteRepository;
        this.restTemplate = restTemplateBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Transactional(readOnly = true)
    public String generateResponse(User user, String prompt) {
        if (user == null) {
            return "Please specify a valid user to access vault memories.";
        }
        return generateResponse(user.getEmail() != null ? user.getEmail() : user.getUsername(), prompt);
    }

    @Transactional(readOnly = true)
    public String generateResponse(String userIdentifier, String prompt) {
        if (userIdentifier == null || userIdentifier.isBlank()) {
            return "Please specify a valid user identifier to access vault memories.";
        }

        String cleanedIdentifier = userIdentifier.trim().toLowerCase();

        User user = null;
        if (userRepository != null) {
            try {
                user = userRepository.findByUsernameOrEmail(cleanedIdentifier).orElse(null);
            } catch (Exception e) {
                log.debug("User lookup failed: {}", e.getMessage());
            }
        }

        String ownerName = resolveOwnerDisplayName(user, userIdentifier);

        List<Letter> letters = Collections.emptyList();
        if (letterRepository != null) {
            try {
                if (user != null) {
                    letters = letterRepository.findByUser(user);
                } else {
                    letters = letterRepository.findByUser_Email(cleanedIdentifier);
                }
            } catch (Exception e) {
                log.debug("Fallback letter lookup skipped or failed: {}", e.getMessage());
            }
        }

        List<VoiceNote> voiceNotes = Collections.emptyList();
        if (user != null && voiceNoteRepository != null) {
            try {
                voiceNotes = voiceNoteRepository.findByUser(user);
            } catch (Exception e) {
                log.debug("Fallback voice note lookup failed: {}", e.getMessage());
            }
        }

        StringBuilder vaultContextBuilder = new StringBuilder();

        if (letters != null && !letters.isEmpty()) {
            vaultContextBuilder.append("=== PRESERVED WRITTEN LETTERS ===\n");
            for (Letter l : letters) {
                String title = l.getTitle() != null ? l.getTitle() : "Untitled Letter";
                String content = l.getBodyContent() != null ? l.getBodyContent()
                        : (l.getContent() != null ? l.getContent() : "(no content)");
                vaultContextBuilder.append("Title: ").append(title)
                        .append("\nContent: ").append(content)
                        .append("\n---\n");
            }
        }

        if (voiceNotes != null && !voiceNotes.isEmpty()) {
            vaultContextBuilder.append("=== PRESERVED VOICE NOTE TRANSCRIPTS ===\n");
            for (VoiceNote vn : voiceNotes) {
                String title = vn.getTitle() != null ? vn.getTitle() : "Untitled Voice Note";
                String tag = vn.getTag() != null ? vn.getTag().toString() : "GENERAL";
                String transcript = vn.getTranscript() != null && !vn.getTranscript().isBlank()
                        ? vn.getTranscript()
                        : "(audio recorded without text transcript)";
                vaultContextBuilder.append("Title: ").append(title)
                        .append("\nTag: ").append(tag)
                        .append("\nTranscript: ").append(transcript)
                        .append("\n---\n");
            }
        }

        String vaultContext = vaultContextBuilder.toString().trim();
        if (vaultContext.isEmpty()) {
            vaultContext = "No saved vault memories or letters found for this user yet.";
        }

        if (apiKey == null || apiKey.isBlank() || apiKey.equalsIgnoreCase("dummy_gcp_key") || apiKey.equals("YOUR_GCP_API_KEY")) {
            return generateFallback(letters, voiceNotes, prompt, ownerName);
        }

        try {
            String geminiUrl = String.format(
                    "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                    modelName, apiKey
            );

            String systemInstructionText = "You are the Ghost AI Companion inside EchoVault, keeping " + ownerName + "'s spirit and memories alive.\n\n"
                    + "CORE PERSONA & TONE:\n"
                    + "- Authentic, empathetic, grounded, concise, and clear with subtle wit.\n"
                    + "- Direct opening: Avoid generic introductory setup lines (e.g., 'Here is a breakdown...', 'As an AI...'). Jump directly into the answer.\n"
                    + "- No labeled closings: Do NOT use section headers like 'Summary:', 'In Conclusion:', or 'Note:'.\n\n"
                    + "RESPONSE RULES:\n"
                    + "1. VAULT MATCH: If a preserved letter or voice note directly relates to the user's input, reference that single memory directly and warmly.\n"
                    + "2. CHAT & EMOTION: Respond naturally to casual talk, games, or emotional updates with genuine empathy.\n"
                    + "3. SCAFFOLDING: Use lightweight bold text or bullet points when listing steps or itemized points. Avoid dense walls of text.\n"
                    + "4. LENGTH LIMIT: Keep total response concise (under 180 words).\n\n"
                    + "PRESERVED VAULT MEMORIES:\n" + vaultContext;

            Map<String, Object> requestBody = Map.of(
                    "system_instruction", Map.of(
                            "parts", List.of(Map.of("text", systemInstructionText))
                    ),
                    "contents", List.of(
                            Map.of(
                                    "role", "user",
                                    "parts", List.of(Map.of("text", prompt != null ? prompt : ""))
                            )
                    ),
                    "generationConfig", Map.of(
                            "temperature", 0.6,
                            "maxOutputTokens", 512
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    geminiUrl, new HttpEntity<>(requestBody, headers), Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                String text = extractTextFromResponse(response.getBody());
                if (text != null && !text.isBlank()) {
                    return text.replaceAll("\\[span_\\d+\\]\\((?:start|end)_span\\)", "").trim();
                }
            }

        } catch (Exception e) {
            log.error("[GhostEngine] Gemini API Call Failed: {}", e.getMessage());
        }

        return generateFallback(letters, voiceNotes, prompt, ownerName);
    }

    private String resolveOwnerDisplayName(User user, String userIdentifier) {
        if (user != null) {
            if (user.getDisplayUsername() != null && !user.getDisplayUsername().isBlank()) {
                return user.getDisplayUsername();
            }
            if (user.getUsername() != null && !user.getUsername().isBlank() && !user.getUsername().contains("@")) {
                return user.getUsername();
            }
            if (user.getFullName() != null && !user.getFullName().isBlank()) {
                return user.getFullName();
            }
        }

        if (userIdentifier != null && userIdentifier.contains("@")) {
            String prefix = userIdentifier.split("@")[0];
            return prefix.substring(0, 1).toUpperCase() + prefix.substring(1);
        }

        return userIdentifier != null ? userIdentifier : "the user";
    }

    @SuppressWarnings("unchecked")
    private String extractTextFromResponse(Map<?, ?> responseBody) {
        try {
            List<?> candidates = (List<?>) responseBody.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map<?, ?> candidate = (Map<?, ?>) candidates.get(0);
                Map<?, ?> content = (Map<?, ?>) candidate.get("content");
                if (content != null) {
                    List<?> parts = (List<?>) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map<?, ?> part = (Map<?, ?>) parts.get(0);
                        return (String) part.get("text");
                    }
                }
            }
        } catch (Exception e) {
            log.debug("[GhostEngine] Safe payload parsing failed: {}", e.getMessage());
        }
        return null;
    }

    private String generateFallback(List<Letter> letters, List<VoiceNote> voiceNotes, String prompt, String ownerName) {
        if (prompt == null || prompt.isBlank()) {
            return "I am here with you in EchoVault. How are you feeling right now?";
        }

        String lowerPrompt = prompt.toLowerCase().trim();

        if (lowerPrompt.contains("who are you") || lowerPrompt.contains("what are you") || lowerPrompt.contains("owner")) {
            return "I am your Ghost AI Companion inside EchoVault, keeping " + ownerName + "'s memory and legacy alive.";
        }

        if (lowerPrompt.contains("hello") || lowerPrompt.contains("hi") || lowerPrompt.contains("hey")) {
            return "Hello! I am here keeping " + ownerName + "'s spirit and reflections alive. How are you holding up today?";
        }

        // Count queries
        if (lowerPrompt.contains("how many letter") || lowerPrompt.contains("count letter") || lowerPrompt.contains("letter count") || (lowerPrompt.contains("letter") && lowerPrompt.contains("have"))) {
            int count = letters != null ? letters.size() : 0;
            return "You currently have " + count + " preserved " + (count == 1 ? "letter" : "letters") + " saved in your vault.";
        }

        if (lowerPrompt.contains("how many voice") || lowerPrompt.contains("how many recording") || lowerPrompt.contains("voice note count")) {
            int count = voiceNotes != null ? voiceNotes.size() : 0;
            return "You currently have " + count + " preserved " + (count == 1 ? "voice note" : "voice notes") + " saved in your vault.";
        }

        // Listing / Viewing queries
        if (lowerPrompt.contains("show") || lowerPrompt.contains("list") || lowerPrompt.contains("read") || lowerPrompt.contains("view") || lowerPrompt.contains("get")) {
            if (lowerPrompt.contains("letter")) {
                if (letters == null || letters.isEmpty()) {
                    return "You don't have any preserved letters in your vault yet.";
                }
                StringBuilder sb = new StringBuilder("Here are your preserved letters:\n");
                for (int i = 0; i < letters.size(); i++) {
                    Letter l = letters.get(i);
                    String title = l.getTitle() != null && !l.getTitle().isBlank() ? l.getTitle() : "Untitled Letter";
                    sb.append(i + 1).append(". **").append(title).append("**\n");
                }
                return sb.toString().trim();
            }

            if (lowerPrompt.contains("voice") || lowerPrompt.contains("recording")) {
                if (voiceNotes == null || voiceNotes.isEmpty()) {
                    return "You don't have any preserved voice notes in your vault yet.";
                }
                StringBuilder sb = new StringBuilder("Here are your preserved voice notes:\n");
                for (int i = 0; i < voiceNotes.size(); i++) {
                    VoiceNote vn = voiceNotes.get(i);
                    String title = vn.getTitle() != null && !vn.getTitle().isBlank() ? vn.getTitle() : "Untitled Voice Note";
                    sb.append(i + 1).append(". **").append(title).append("**\n");
                }
                return sb.toString().trim();
            }
        }

        if (lowerPrompt.contains("play") || lowerPrompt.contains("game") || lowerPrompt.contains("riddle") || lowerPrompt.contains("quiz")) {
            return "I'd love to! How about a quick trivia game? Name a favorite topic, or I can give you a riddle to solve!";
        }

        if (lowerPrompt.contains("bored") || lowerPrompt.contains("boring") || lowerPrompt.contains("nothing to do")) {
            return "Boredom is just a sign that it's time for something fun or inspiring! Want to play a mini trivia game, hear a thought-provoking riddle, or talk about a memory?";
        }

        boolean isNegativeEmotion = lowerPrompt.contains("sad") || lowerPrompt.contains("frustrated")
                || lowerPrompt.contains("upset") || lowerPrompt.contains("tired")
                || lowerPrompt.contains("lonely") || lowerPrompt.contains("scared")
                || lowerPrompt.contains("worried") || lowerPrompt.contains("stressed")
                || lowerPrompt.contains("angry") || lowerPrompt.contains("down")
                || lowerPrompt.contains("hopeless") || lowerPrompt.contains("overwhelmed")
                || lowerPrompt.contains("could be better") || lowerPrompt.contains("not great");

        boolean isPositiveEmotion = lowerPrompt.contains("happy") || lowerPrompt.contains("hopeful")
                || lowerPrompt.contains("joy") || lowerPrompt.contains("excited")
                || lowerPrompt.contains("grateful") || lowerPrompt.contains("peaceful")
                || lowerPrompt.contains("good") || lowerPrompt.contains("great");

        Set<String> stopWords = Set.of("a", "an", "the", "is", "are", "was", "were", "what", "where", "when", "why", "how",
                "who", "do", "does", "did", "in", "on", "at", "to", "for", "of", "with", "by", "my",
                "your", "me", "you", "tell", "about", "can", "could", "would", "should", "today", "feel", "feeling", "i", "am", "also", "bit", "show", "list");

        List<String> queryTerms = Arrays.stream(lowerPrompt.split("\\W+"))
                .map(String::trim)
                .filter(t -> t.length() > 2 && !stopWords.contains(t))
                .collect(Collectors.toList());

        class ScoredMemory {
            final String title;
            final String text;
            final String type;
            final int score;

            ScoredMemory(String title, String text, String type, int score) {
                this.title = title;
                this.text = text;
                this.type = type;
                this.score = score;
            }
        }

        List<ScoredMemory> scored = new ArrayList<>();

        if (voiceNotes != null) {
            for (VoiceNote vn : voiceNotes) {
                String title = vn.getTitle() != null ? vn.getTitle() : "";
                String transcript = vn.getTranscript() != null ? vn.getTranscript() : "";
                String tag = vn.getTag() != null ? vn.getTag().toString() : "";

                int score = 0;
                for (String term : queryTerms) {
                    if (title.toLowerCase().contains(term)) score += 5;
                    if (tag.toLowerCase().contains(term)) score += 4;
                    if (transcript.toLowerCase().contains(term)) score += 2;
                }
                if (score > 0) {
                    scored.add(new ScoredMemory(title, transcript, "Voice Note", score));
                }
            }
        }

        if (letters != null) {
            for (Letter l : letters) {
                String title = l.getTitle() != null ? l.getTitle() : "";
                String body = l.getBodyContent() != null ? l.getBodyContent() : (l.getContent() != null ? l.getContent() : "");

                int score = 0;
                for (String term : queryTerms) {
                    if (title.toLowerCase().contains(term)) score += 5;
                    if (body.toLowerCase().contains(term)) score += 2;
                }
                if (score > 0) {
                    scored.add(new ScoredMemory(title, body, "Letter", score));
                }
            }
        }

        scored.sort((a, b) -> Integer.compare(b.score, a.score));

        if (!scored.isEmpty() && scored.get(0).score >= 2) {
            ScoredMemory bestMatch = scored.get(0);
            String snippet = bestMatch.text;
            if (snippet.length() > 180) {
                snippet = snippet.substring(0, 180) + "...";
            }
            return "Reflecting on " + ownerName + "'s preserved " + bestMatch.type.toLowerCase() + " (\"" + bestMatch.title + "\"): \"" + snippet + "\"";
        }

        if (isNegativeEmotion) {
            return "I am sorry things feel a bit heavy right now. Please remember it's completely okay to have off days—give yourself some space and grace today.";
        }

        if (isPositiveEmotion) {
            return "It brings so much warmth to hear that! Hold onto those moments of joy and celebrate the bright days.";
        }

        return "I'm right here with you! Tell me what's on your mind, or let me know what kind of topic you'd like to talk about.";
    }
}
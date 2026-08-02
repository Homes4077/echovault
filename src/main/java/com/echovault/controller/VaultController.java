package com.echovault.controller;

import com.echovault.dto.LetterRequestDto;
import com.echovault.model.*;
import com.echovault.repository.*;
import com.echovault.service.AssemblyAiService;
import com.echovault.service.CloudinaryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

@Controller
@RequestMapping("/vault")
public class VaultController {

    @Autowired private VoiceNoteRepository voiceNoteRepository;
    @Autowired private LetterRepository letterRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CloudinaryService cloudinaryService;
    @Autowired private AssemblyAiService assemblyAiService;

    @PostMapping("/voice/upload")
    @ResponseBody
    public String uploadVoiceNote(@RequestParam("userId") Long userId,
                                  @RequestParam("title") String title,
                                  @RequestParam("tag") VoiceNote.Tag tag,
                                  @RequestParam("file") MultipartFile file) throws Exception {
        User user = userRepository.findById(userId).orElseThrow();

        // 1. Upload audio binary to Cloudinary
        Map<String, String> uploadMap = cloudinaryService.uploadFile(file, "voice_notes");

        // 2. Transcribe voice note using AssemblyAI
        String transcript = assemblyAiService.transcribeFromUrl(uploadMap.get("url"));

        // 3. Save VoiceNote record
        VoiceNote note = new VoiceNote();
        note.setUser(user);
        note.setTitle(title);
        note.setTag(tag);
        note.setCloudinaryUrl(uploadMap.get("url"));
        note.setCloudinaryPublicId(uploadMap.get("public_id"));
        note.setTranscription(transcript);
        note.setTranscriptionStatus(VoiceNote.TranscriptionStatus.COMPLETED);
        voiceNoteRepository.save(note);

        return "VOICE_NOTE_SAVED";
    }

    @PostMapping("/letter/compose")
    @ResponseBody
    public String composeLetter(@RequestBody LetterRequestDto dto) {
        User user = userRepository.findById(dto.getUserId()).orElseThrow();

        Letter letter = new Letter();
        letter.setUser(user);
        letter.setRecipientName(dto.getRecipientName());
        letter.setRecipientEmail(dto.getRecipientEmail());
        letter.setSubject(dto.getSubject());
        letter.setBodyContent(dto.getBodyContent());
        letter.setTag(dto.getTag());
        letter.setScheduledDeliveryAt(dto.getScheduledDeliveryAt());
        letterRepository.save(letter);

        return "LETTER_SCHEDULED";
    }
}

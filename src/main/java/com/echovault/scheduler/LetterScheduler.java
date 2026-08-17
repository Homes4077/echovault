package com.echovault.scheduler;

import com.echovault.model.Letter;
import com.echovault.repository.LetterRepository;
import com.echovault.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LetterScheduler {

    private final LetterRepository letterRepository;
    private final EmailService emailService;

    @Scheduled(fixedRate = 60000)
    public void processDueLetters() {
        LocalDateTime now = LocalDateTime.now();
        List<Letter> dueLetters = letterRepository.findAllByIsDeliveredFalseAndScheduledDeliveryAtBefore(now);

        if (!dueLetters.isEmpty()) {
            log.info("Found {} due letters ready for unlock and email dispatch.", dueLetters.size());
            for (Letter letter : dueLetters) {
                letter.setDelivered(true);
                letterRepository.save(letter);

                if (letter.getRecipientEmail() != null && !letter.getRecipientEmail().isBlank()) {
                    emailService.sendScheduledLetter(
                            letter.getRecipientEmail(),
                            letter.getSubject(),
                            letter.getContent(),
                            letter.getRecipientName()
                    );
                }
            }
        }
    }
}

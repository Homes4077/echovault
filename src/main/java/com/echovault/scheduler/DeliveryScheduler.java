package com.echovault.scheduler;

import com.echovault.model.DeliveryLog;
import com.echovault.model.Letter;
import com.echovault.repository.DeliveryLogRepository;
import com.echovault.repository.LetterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DeliveryScheduler {

    private final LetterRepository letterRepository;
    private final DeliveryLogRepository deliveryLogRepository;

    @Scheduled(fixedRate = 60000)
    public int executePendingDeliveries() {
        List<Letter> pendingLetters = letterRepository.findAllByIsDeliveredFalseAndScheduledDeliveryAtBefore(LocalDateTime.now());
        int processedCount = 0;

        for (Letter letter : pendingLetters) {
            boolean sent = true;

            if (sent) {
                letter.setDelivered(true);
                letter.setDeliveredAt(LocalDateTime.now());
                letterRepository.save(letter);
            }

            DeliveryLog log = DeliveryLog.builder()
                    .user(letter.getUser())
                    .deliveryType(DeliveryLog.DeliveryType.EMAIL)
                    .recipient(letter.getRecipientEmail())
                    .triggerReason("Scheduled Delivery")
                    .status(sent ? DeliveryLog.Status.SENT : DeliveryLog.Status.FAILED)
                    .timestamp(LocalDateTime.now())
                    .build();

            deliveryLogRepository.save(log);
            processedCount++;
        }

        return processedCount;
    }
}

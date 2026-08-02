package com.echovault.scheduler;

import com.echovault.model.*;
import com.echovault.repository.*;
import com.echovault.service.SendGridEmailService;
import com.echovault.service.TwilioSmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class DeliveryScheduler {

    @Autowired private LetterRepository letterRepository;
    @Autowired private GhostTriggerRepository ghostTriggerRepository;
    @Autowired private SendGridEmailService sendGridEmailService;
    @Autowired private TwilioSmsService twilioSmsService;
    @Autowired private DeliveryLogRepository deliveryLogRepository;

    // Runs every minute to check exact DateTime triggers
    @Scheduled(cron = "0 * * * * *")
    public void processScheduledDeliveries() {
        executePendingDeliveries();
    }

    public int executePendingDeliveries() {
        LocalDateTime now = LocalDateTime.now();
        List<Letter> pendingLetters = letterRepository.findPendingLettersToDeliver(now);
        int processed = 0;

        for (Letter letter : pendingLetters) {
            boolean sent = sendGridEmailService.sendEmail(
                letter.getRecipientEmail(),
                letter.getSubject(),
                letter.getBodyContent()
            );

            letter.setIsDelivered(sent);
            letter.setDeliveredAt(now);
            letterRepository.save(letter);

            DeliveryLog log = new DeliveryLog();
            log.setUser(letter.getUser());
            log.setDeliveryType(DeliveryLog.DeliveryType.SENDGRID_EMAIL);
            log.setRecipient(letter.getRecipientEmail());
            log.setTriggerReason("SCHEDULED_TIME_LOCKED_LETTER");
            log.setStatus(sent ? DeliveryLog.Status.SUCCESS : DeliveryLog.Status.FAILED);
            deliveryLogRepository.save(log);

            processed++;
        }
        return processed;
    }
}

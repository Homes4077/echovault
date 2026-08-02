package com.echovault.scheduler;

import com.echovault.model.*;
import com.echovault.repository.*;
import com.echovault.service.TwilioSmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class InactivityScheduler {

    @Autowired private UserRepository userRepository;
    @Autowired private FamilyMemberRepository familyMemberRepository;
    @Autowired private TwilioSmsService twilioSmsService;
    @Autowired private DeliveryLogRepository deliveryLogRepository;

    // Daily job at 6:00 AM
    @Scheduled(cron = "0 0 6 * * *")
    public void checkInactivity() {
        executeInactivityChecks();
    }

    public int executeInactivityChecks() {
        int alertCount = 0;
        List<User> users = userRepository.findAll();

        for (User user : users) {
            LocalDateTime threshold = LocalDateTime.now().minusDays(user.getInactivityThresholdDays());
            
            if (user.getLastLoginAt().isBefore(threshold) && !user.getInactivityAlertSent()) {
                List<FamilyMember> emergencyContacts = familyMemberRepository.findByOwnerIdAndPermissionLevel(
                    user.getId(), FamilyMember.PermissionLevel.EMERGENCY_CONTACT
                );

                for (FamilyMember contact : emergencyContacts) {
                    String msg = String.format("EchoVault Alert: Account owner %s has been inactive for over %d days. You are receiving this as their designated emergency contact.", user.getFullName(), user.getInactivityThresholdDays());
                    
                    boolean sent = twilioSmsService.sendSms(contact.getPhoneNumber(), msg);

                    DeliveryLog log = new DeliveryLog();
                    log.setUser(user);
                    log.setDeliveryType(DeliveryLog.DeliveryType.TWILIO_SMS);
                    log.setRecipient(contact.getPhoneNumber());
                    log.setTriggerReason("INACTIVITY_ALERT");
                    log.setStatus(sent ? DeliveryLog.Status.SUCCESS : DeliveryLog.Status.FAILED);
                    deliveryLogRepository.save(log);
                }

                user.setInactivityAlertSent(true);
                userRepository.save(user);
                alertCount++;
            }
        }
        return alertCount;
    }
}

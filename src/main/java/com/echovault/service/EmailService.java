package com.echovault.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Value("${sendgrid.api-key:${SENDGRID_API_KEY:}}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email:${SENDGRID_FROM_EMAIL:jmgitahi590@gmail.com}}")
    private String fromEmail;

    /**
     * Synchronous send method that returns delivery status to prevent premature DB updates.
     */
    public boolean sendScheduledLetter(String toEmail, String subject, String content, String recipientName) {
        if (sendGridApiKey == null || sendGridApiKey.isBlank() || sendGridApiKey.contains("dummy")) {
            log.error("SendGrid dispatch aborted: SENDGRID_API_KEY is missing or configured with a dummy value.");
            return false;
        }

        try {
            Email from = new Email(fromEmail, "EchoVault");
            Email to = new Email(toEmail);
            String body = "Hello " + (recipientName != null && !recipientName.isBlank() ? recipientName : "there") + ",\n\n"
                        + "A time-locked message has unlocked for you on EchoVault:\n\n"
                        + content + "\n\n"
                        + "— Delivered by EchoVault";

            Content mailContent = new Content("text/plain", body);
            Mail mail = new Mail(from, "EchoVault Memory Released: " + subject, to, mailContent);

            SendGrid sg = new SendGrid(sendGridApiKey.trim());
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                log.info("SendGrid email dispatched successfully (Status {}) to recipient: {}", response.getStatusCode(), toEmail);
                return true;
            } else {
                log.error("SendGrid dispatch failed with HTTP {}! Response Body: {}", response.getStatusCode(), response.getBody());
                return false;
            }
        } catch (Exception ex) {
            log.error("Failed to execute SendGrid API call for {}: {}", toEmail, ex.getMessage(), ex);
            return false;
        }
    }
}
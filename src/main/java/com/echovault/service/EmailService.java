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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Value("${sendgrid.api-key:${SENDGRID_API_KEY:}}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email:${SENDGRID_FROM_EMAIL:jmgitahi590@gmail.com}}")
    private String fromEmail;

    @Async
    public void sendScheduledLetter(String toEmail, String subject, String content, String recipientName) {
        // Skip API calls if the key is empty, null, or using the default dummy placeholder
        if (sendGridApiKey == null || sendGridApiKey.isBlank() || sendGridApiKey.contains("dummy")) {
            log.warn("SendGrid dispatch skipped: SENDGRID_API_KEY is unconfigured or set to dummy placeholder.");
            return;
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

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();

            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);
            log.info("SendGrid email dispatch status: {} for recipient: {}", response.getStatusCode(), toEmail);
        } catch (Exception ex) {
            log.error("Failed to send SendGrid email to {}: {}", toEmail, ex.getMessage(), ex);
        }
    }
}

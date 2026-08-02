package com.echovault.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class SendGridEmailService {

    @Value("${spring.sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${echovault.email.sender}")
    private String senderEmail;

    public boolean sendEmail(String recipientEmail, String subjectText, String bodyText) {
        Email from = new Email(senderEmail, "EchoVault Legacy System");
        Email to = new Email(recipientEmail);
        Content content = new Content("text/html", bodyText);
        Mail mail = new Mail(from, subjectText, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            return response.getStatusCode() >= 200 && response.getStatusCode() < 300;
        } catch (IOException ex) {
            return false;
        }
    }
}

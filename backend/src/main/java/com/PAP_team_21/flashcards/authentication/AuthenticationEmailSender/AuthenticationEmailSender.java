package com.PAP_team_21.flashcards.authentication.AuthenticationEmailSender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationEmailSender {
    private final JavaMailSender mailSender;

    @Value("${links.verify-user}")
    private String verifyUserLink;

    @Value("${links.verify-folder-share}")
    private String verifyFolderShareLink;

    @Async
    public void sendVerificationLink(String email, String verificationCode)
    {
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Flashcards - Verification code");
            helper.setText("Click this link to verify your account: " + verifyUserLink + email + "/" + verificationCode);

            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Async
    public void sendVerificationCodeEmail(String email, String verificationCode)
    {
        try{
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(email);
            helper.setSubject("Flashcards - Verification code");
            helper.setText("Your verification code is: " + verificationCode);

            mailSender.send(message);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}

package com.hms.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${app.reset-url}")
    private String resetUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
                String resetLink = resetUrl + "?token=" + token;
                String html = """
                                <!doctype html>
                                <html lang="en">
                                <body style="margin:0;background:#f4f7fb;font-family:Arial,sans-serif;color:#172033;">
                                    <div style="max-width:560px;margin:32px auto;padding:0 16px;">
                                        <div style="background:#176b87;padding:24px 32px;color:#fff;border-radius:12px 12px 0 0;">
                                            <h1 style="margin:0;font-size:24px;">Sanraksha</h1>
                                            <p style="margin:8px 0 0;color:#dff5fb;">Hospital Management System</p>
                                        </div>
                                        <div style="background:#fff;padding:32px;border-radius:0 0 12px 12px;box-shadow:0 4px 18px #17203318;">
                                            <h2 style="margin-top:0;">Reset your password</h2>
                                            <p>We received a request to reset the password for your account.</p>
                                            <p style="margin:28px 0;text-align:center;">
                                                <a href="%s" style="display:inline-block;background:#176b87;color:#fff;text-decoration:none;padding:14px 24px;border-radius:7px;font-weight:bold;">Reset password</a>
                                            </p>
                                            <p style="font-size:14px;color:#526174;">This link expires in 30 minutes and can only be used once.</p>
                                            <p style="font-size:13px;color:#718096;word-break:break-all;">If the button does not work, open: %s</p>
                                            <hr style="border:0;border-top:1px solid #e6ebf1;margin:24px 0;">
                                            <p style="font-size:13px;color:#718096;margin-bottom:0;">If you did not request this, you can safely ignore this email.</p>
                                        </div>
                                    </div>
                                </body>
                                </html>
                                """.formatted(resetLink, resetLink);

                try {
                        MimeMessage message = mailSender.createMimeMessage();
                        MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
                        helper.setTo(toEmail);
                        helper.setSubject("Reset your Sanraksha password");
                        helper.setText(html, true);
                        mailSender.send(message);
                } catch (MessagingException exception) {
                        throw new IllegalStateException("Unable to create password reset email", exception);
                }
    }

    public void sendWelcomeEmail(String toEmail, String fullName) {
        log.info("Sending welcome email to {}", toEmail);
        String html = """
                <!doctype html>
                <html lang="en">
                <body style="margin:0;background:#f4f7fb;font-family:Arial,sans-serif;color:#172033;">
                    <div style="max-width:560px;margin:32px auto;padding:0 16px;">
                        <div style="background:#176b87;padding:24px 32px;color:#fff;border-radius:12px 12px 0 0;">
                            <h1 style="margin:0;font-size:24px;">Sanraksha</h1>
                            <p style="margin:8px 0 0;color:#dff5fb;">Hospital Management System</p>
                        </div>
                        <div style="background:#fff;padding:32px;border-radius:0 0 12px 12px;box-shadow:0 4px 18px #17203318;">
                            <h2 style="margin-top:0;">Welcome, %s!</h2>
                            <p>Your Sanraksha account has been created successfully.</p>
                            <p>You can now sign in to manage your healthcare appointments and records.</p>
                            <hr style="border:0;border-top:1px solid #e6ebf1;margin:24px 0;">
                            <p style="font-size:13px;color:#718096;margin-bottom:0;">If you did not create this account, please contact support.</p>
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(fullName);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject("Welcome to Sanraksha");
            helper.setText(html, true);
            mailSender.send(message);
            log.info("Welcome email sent successfully to {}", toEmail);
        } catch (MessagingException exception) {
            log.error("Unable to create welcome email for {}", toEmail, exception);
            throw new IllegalStateException("Unable to create welcome email", exception);
        } catch (RuntimeException exception) {
            log.error("SMTP failed while sending welcome email to {}", toEmail, exception);
            throw exception;
        }
    }
}
package com.thbs.usercreation.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    @Autowired
	private JavaMailSender mailSender;

	public void sendEmail(String to, String subject, String content) {
        MimeMessage message = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom("lms.torryharris@gmail.com");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); 
            mailSender.send(message);
        } catch (MessagingException e) {
			System.out.println("mail can't be sent..");
		}
	}
	
	
	public void sendpasswordurl(String email, String url) {
        // String resetLink = "http://localhost:1111/reset?token=" + token;
        String subject = "forgot password";
 
        // Relative path to the logo in your project's static resources
        // String logoPath = "https://github.com/RiteshRaoV/project_final/blob/main/src/main/resources/static/Images/logo.png?raw=true";
 
        // Email body with logo on top
        String body = "<!DOCTYPE html>"
                + "<html lang=\"en\">"
                + "<head>"
                + "<meta charset=\"UTF-8\">"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">"
                + "<title>Email Template</title>"
                + "<style>"
                + "body { font-family: Arial, sans-serif; margin: 0; padding: 0; background-color: #f5f5f5; }"
                + ".container { max-width: 600px; margin: 0 auto; padding: 20px; background-color: #fff; border-radius: 10px; box-shadow: 0 0 10px rgba(0, 0, 0, 0.1); }"
                + ".logo { display: block; margin: 0 auto; max-width: 200px; height: auto; }"
                + ".content { padding: 20px; text-align: center; }"
                + "</style>"
                + "</head>"
                + "<body>"
                + "<div class=\"container\">"
                + "<div class=\"content\">"
                + "<h2>forgot  password</h2>"
                + "<p> click the following link for changing password:</p>"
                + "<p><a href=\"" + url + "\">change password</a></p>"
                + "</div>"
                + "</div>"
                + "</body>"
                + "</html>";
 
        sendEmail(email, subject, body);
    }
}


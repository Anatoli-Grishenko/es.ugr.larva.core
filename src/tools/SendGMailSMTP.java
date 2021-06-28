/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import java.io.File;
import java.io.IOException;
import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Uses app passwords for GMail API
 * https://support.google.com/accounts/answer/185833?hl=es#zippy=%2Cpor-qu%C3%A9-es-posible-que-necesites-una-contrase%C3%B1a-de-aplicaci%C3%B3n
 *
 * @author lcv
 */
public class SendGMailSMTP {

    protected String to, from, host, username, appPassowrd;
    protected Properties properties;

    public SendGMailSMTP(String username, String password) {
        // Assuming you are sending email from through gmails smtp
        host = "smtp.gmail.com";

        // Get system properties
        properties = System.getProperties();

        // Setup mail server
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "465");
        properties.put("mail.smtp.ssl.enable", "true");
        properties.put("mail.smtp.auth", "true");
        this.username = username;
        this.appPassowrd = password;
        from = username;
    }

    public SendGMailSMTP sendGMail(String receiver, String subject, String body) throws AddressException, MessagingException {
        // Recipient's email ID needs to be mentioned.
        to = receiver;

        // Get the Session object.// and pass username and appPassowrd
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, appPassowrd);
            }
        });

        // Used to debug SMTP issues
        session.setDebug(true);

        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Set From: header field of the header.
        message.setFrom(new InternetAddress(from));

        // Set To: header field of the header.
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

        // Set Subject: header field
        message.setSubject(subject);

        // Now set the actual message
        message.setText(body);

        // Send message
        Transport.send(message);
        return this;
    }

    public SendGMailSMTP sendGMail(String receiver, String subject, String body, String filename) throws AddressException, MessagingException, IOException {
        // Recipient's email ID needs to be mentioned.
        to = receiver;

        // Get the Session object.// and pass username and appPassowrd
        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, appPassowrd);
            }
        });

        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Set From: header field of the header.
        message.setFrom(new InternetAddress(from));

        // Set To: header field of the header.
        message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

        // Set Subject: header field
        message.setSubject(subject);

        Multipart multipart = new MimeMultipart();

        MimeBodyPart attachmentPart = new MimeBodyPart();

        MimeBodyPart textPart = new MimeBodyPart();

        File f = new File(filename);

        attachmentPart.attachFile(f);
        textPart.setText(body);
        multipart.addBodyPart(textPart);
        multipart.addBodyPart(attachmentPart);

        message.setContent(multipart);

        // Send message
        Transport.send(message);
        return this;
    }

}

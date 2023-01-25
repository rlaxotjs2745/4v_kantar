package com.kantar.util;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.mail.*;
import jakarta.mail.internet.*;

@Component
public class MailSender {
    @Value("${mail.passwd}")
    private String mailpasswd;

    private String FROM = "kantar@kantar.co.kr";
    private String FROMNAME = "KANTAR";
    private String SMTP_USERNAME = "seeshow202106@gmail.com";
    private String HOST = "smtp.gmail.com";
    private int PORT = 587;

    public void sender(String To, String Subject, String Body) throws Exception {
        Properties props = System.getProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.port", PORT);
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.ssl.trust", HOST);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        Session session = Session.getDefaultInstance(props);
        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(FROM, FROMNAME));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(To));
        msg.setSubject(Subject);
        msg.setContent(Body, "text/html;charset=utf-8");

        Transport transport = session.getTransport();
        try {
            transport.connect(HOST, SMTP_USERNAME, mailpasswd);
            transport.sendMessage(msg, msg.getAllRecipients());
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            transport.close();
        }
    }
}

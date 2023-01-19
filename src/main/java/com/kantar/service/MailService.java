package com.kantar.service;

import com.kantar.vo.UserVO;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;



@RequiredArgsConstructor
@Service
public class MailService {

    private JavaMailSender mailSender;
    private static final String FROM_ADDRESS = "kantar@kantar.co.kr";

    public void mailSend(UserVO userVO) throws MessagingException {
        MimeMessage mail = mailSender.createMimeMessage();
        MimeMessageHelper mailHelper = new MimeMessageHelper(mail, true, "UTF-8");

        mailHelper.setFrom(FROM_ADDRESS);
        mailHelper.setTo(userVO.getUser_id());
        mailHelper.setSubject("[KANTAR] 회원가입 안내");
        mailHelper.setText(userVO.getUser_pw(), true);

        mailSender.send(mail);
    }
}

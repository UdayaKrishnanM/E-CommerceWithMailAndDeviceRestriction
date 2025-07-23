package com.ecommerce.demo.email.services;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Service;
import com.ecommerce.demo.email.dto.MailBody;

@Service
public class EmailServices {

	
	@Autowired
	public JavaMailSenderImpl javaMailSender;
	
	public void sendSimpleMessage(MailBody mailBody) {
		try {			
			SimpleMailMessage message = new SimpleMailMessage();
			
//			message.setTo(mailBody.to());
//			message.setFrom("udayakrishnan.ece.sec@gmail.com");
//			message.setSubject(mailBody.subject());
//			message.setText(mailBody.text());
			
			message.setTo("udayakrishnan2001@gmail.com");
			message.setFrom("udayakrishnan.ece.sec@gmail.com");
			message.setSubject(mailBody.subject());
			message.setText(mailBody.text());
			javaMailSender.send(message);
		} catch (MailException e) {
			LoggerFactory.getLogger(getClass()).error("Error sending email: ", e);
		}


	}
	
}

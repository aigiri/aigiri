package io.kutumbini.test;

import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.stereotype.Component;

@Component
public class TestConfig {
	
	// tests do not needs this 
	// it is here to suppress a runtime error looking for and not finding this bean
	@Bean 
	public JavaMailSender javaMailSender() {
		return new JavaMailSenderImpl();
	}

}
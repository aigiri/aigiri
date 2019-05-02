package io.kutumbini.web.controller;

import java.util.Locale;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.auth.persistence.model.VerificationToken;
import io.kutumbini.auth.service.IUserService;
import io.kutumbini.auth.web.util.GenericResponse;

@Controller
public class UserController {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	private IUserService userService;

	@Autowired
	private MessageSource messages;

	@Autowired
	private JavaMailSender mailSender;

	@Autowired
	private Environment env;

	public UserController() {
		super();
	}

	/**
	 * 
	 * @param fromName    the name of the sender
	 * @param emailTo     the email of the user ( or potential user )
	 * @param accessLevel ( Peripheral or Full )
	 * @return
	 */
	@GetMapping("/user/grantFulfill")
	@ResponseBody
	public GenericResponse grantFulfill(final HttpServletRequest request, String token) {
		final String result = userService.validateVerificationToken(token);
		if (result.equals("valid")) {
			return new GenericResponse(
					messages.getMessage("message.grant.sent.confirmation", null, request.getLocale()));			
		}
		else {
			return new GenericResponse(
					messages.getMessage("message.grant.sent.confirmation", null, request.getLocale()));

		}
	}

	/**
	 * 
	 * @param fromName    the name of the sender
	 * @param emailTo     the email of the user ( or potential user )
	 * @param accessLevel ( Peripheral or Full )
	 * @return
	 */
	@GetMapping("/user/grantAccess")
	@ResponseBody
	public GenericResponse grantAccessToAnotherUser(final HttpServletRequest request, String fromName, String emailTo,
			String accessLevel) {
		final VerificationToken newToken = userService.createVerificationTokenForUser(getUser(),
				UUID.randomUUID().toString());
		mailSender.send(constructGrantAuthorityVerificationTokenEmail(getAppUrl(request), request.getLocale(), newToken,
				fromName, emailTo));
		return new GenericResponse(
				messages.getMessage("message.grant.sent.confirmation", new String[] { emailTo }, request.getLocale()));
	}

	// ============== NON-API ============

	private SimpleMailMessage constructGrantAuthorityVerificationTokenEmail(final String contextPath,
			final Locale locale, final VerificationToken newToken, final String fromName, final String emailTo) {
		final String grantFulfillUrl = contextPath + "/user/grantFulfill?token=" + newToken.getToken();
		final String message = messages.getMessage("message.for.grant.recipient", new String[] { fromName }, locale);
		return constructEmail("An invitation from Kutumbini on behalf of " + fromName
				+ " to create/extend your family tree " + fromName, message + " \r\n" + grantFulfillUrl, emailTo);
	}

	private SimpleMailMessage constructEmail(String subject, String body, String emailTo) {
		final SimpleMailMessage smMessage = new SimpleMailMessage();
		smMessage.setSubject(subject);
		smMessage.setText(body);
		smMessage.setTo(emailTo);
		smMessage.setFrom(env.getProperty("support.email"));
		return smMessage;
	}

	private String getAppUrl(HttpServletRequest request) {
		return env.getProperty("url.scheme") + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
	}

	private User getUser() {
		AbstractAuthenticationToken auth = (AbstractAuthenticationToken) SecurityContextHolder.getContext()
				.getAuthentication();
		return (User) auth.getPrincipal();
	}

}

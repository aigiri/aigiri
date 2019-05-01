package io.kutumbini.auth.web.controller;

import java.io.UnsupportedEncodingException;
import java.util.Locale;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.auth.persistence.model.VerificationToken;
//import io.kutumbini.auth.registration.OnRegistrationCompleteEvent;
import io.kutumbini.auth.security.ISecurityUserService;
import io.kutumbini.auth.service.IUserService;
import io.kutumbini.auth.web.dto.PasswordDto;
import io.kutumbini.auth.web.dto.UserDto;
import io.kutumbini.auth.web.error.InvalidOldPasswordException;
import io.kutumbini.auth.web.util.GenericResponse;

/**
 * 
 * MIT License
 * 
 * Copyright (c) 2017 Baeldung
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

@Controller
public class RegistrationController {
	private final Logger LOGGER = LoggerFactory.getLogger(getClass());

	@Autowired
	private IUserService userService;

	@Autowired
	private ISecurityUserService securityUserService;

	@Autowired
	private MessageSource messages;

	@Autowired
	private JavaMailSender mailSender;

//    @Autowired
//    private ApplicationEventPublisher eventPublisher;

	@Autowired
	private Environment env;

	@Autowired
	private AuthenticationManager authenticationManager;

	public RegistrationController() {
		super();
	}

	/**
	 * 
	 * @param email of the user ( or potential user )
	 * @param accessLevel ( Peripheral or Full )
	 * @return
	 */
	@GetMapping("/user/grantAccessToAnotherUser")
	@ResponseBody
	public GenericResponse grantAccessToAnotherUser(final HttpServletRequest request, String email, String accessLevel) {
		final VerificationToken newToken = userService.createVerificationTokenForUser(getUser(), UUID.randomUUID().toString());
		mailSender.send(constructResendVerificationTokenEmail(getAppUrl(request), request.getLocale(), newToken, getUser()));
		return new GenericResponse(messages.getMessage("message.grantAccess.sent", new String[] {email}, request.getLocale()));
	}

	// Registration
	@RequestMapping(value = "/user/registration", method = RequestMethod.POST)
	@ResponseBody
	public GenericResponse registerUserAccount(@Valid final UserDto accountDto, final HttpServletRequest request) {
		LOGGER.debug("Registering user account with information: {}", accountDto);

		final User user = userService.registerNewUserAccount(accountDto);
//        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(registered, request.getLocale(), getAppUrl(request)));
		final String token = UUID.randomUUID().toString();
		userService.createVerificationTokenForUser(user, token);

//		final String confirmationUrl = "/registrationConfirm.html?token=" + token;
		return new GenericResponse("success");
	}

	@RequestMapping(value = "/registrationConfirm", method = RequestMethod.GET)
	public String confirmRegistration(final HttpServletRequest request, final Model model,
			@RequestParam("token") final String token) throws UnsupportedEncodingException {
		Locale locale = request.getLocale();
		final String result = userService.validateVerificationToken(token);
		if (result.equals("valid")) {
//			final User user = userService.getUser(token);
			// if (user.isUsing2FA()) {
			// model.addAttribute("qr", userService.generateQRUrl(user));
			// return "redirect:/qrcode.html?lang=" + locale.getLanguage();
			// }
			model.addAttribute("message", messages.getMessage("message.accountVerified", null, locale));
			return "redirect:/console.html?lang=" + locale.getLanguage();
		}

		model.addAttribute("message", messages.getMessage("auth.message." + result, null, locale));
		model.addAttribute("expired", "expired".equals(result));
		model.addAttribute("token", token);
		return "redirect:/badUser.html?lang=" + locale.getLanguage();
	}

	// user activation - verification

	@RequestMapping(value = "/user/resendRegistrationToken", method = RequestMethod.GET)
	@ResponseBody
	public GenericResponse resendRegistrationToken(final HttpServletRequest request,
			@RequestParam("token") final String existingToken) {
		final VerificationToken newToken = userService.generateNewVerificationToken(existingToken);
		final User user = userService.getUser(newToken.getToken());
		mailSender.send(constructResendVerificationTokenEmail(getAppUrl(request), request.getLocale(), newToken, user));
		return new GenericResponse(messages.getMessage("message.resendToken", null, request.getLocale()));
	}

	// Reset password
	@RequestMapping(value = "/user/resetPassword", method = RequestMethod.POST)
	@ResponseBody
	public GenericResponse resetPassword(final HttpServletRequest request,
			@RequestParam("email") final String userEmail) {
		final User user = userService.findUserByEmail(userEmail);
		if (user != null) {
			final String token = UUID.randomUUID().toString();
			userService.createPasswordResetTokenForUser(user, token);
			mailSender.send(constructResetTokenEmail(getAppUrl(request), request.getLocale(), token, user));
		}
		return new GenericResponse(messages.getMessage("message.resetPasswordEmail", null, request.getLocale()));
	}

	@RequestMapping(value = "/user/changePassword", method = RequestMethod.GET)
	public String showChangePasswordPage(final Locale locale, final Model model, @RequestParam("id") final long id,
			@RequestParam("token") final String token) {
		final String result = securityUserService.validatePasswordResetToken(id, token);
		if (result != null) {
			model.addAttribute("message", messages.getMessage("auth.message." + result, null, locale));
			return "redirect:/login?lang=" + locale.getLanguage();
		}
		return "redirect:/updatePassword.html?lang=" + locale.getLanguage();
	}

	@RequestMapping(value = "/user/savePassword", method = RequestMethod.POST)
	@ResponseBody
	public GenericResponse savePassword(final Locale locale, @Valid PasswordDto passwordDto) {
		final User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		userService.changeUserPassword(user, passwordDto.getNewPassword());
		return new GenericResponse(messages.getMessage("message.resetPasswordSuc", null, locale));
	}

	// change user password
	@RequestMapping(value = "/user/updatePassword", method = RequestMethod.POST)
	@ResponseBody
	public GenericResponse changeUserPassword(final Locale locale, @Valid PasswordDto passwordDto) {
		final User user = userService.findUserByEmail(
				((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getEmail());
		if (!userService.checkIfValidOldPassword(user, passwordDto.getOldPassword())) {
			throw new InvalidOldPasswordException();
		}
		userService.changeUserPassword(user, passwordDto.getNewPassword());
		return new GenericResponse(messages.getMessage("message.updatePasswordSuc", null, locale));
	}

	@RequestMapping(value = "/user/update/2fa", method = RequestMethod.POST)
	@ResponseBody
	public GenericResponse modifyUser2FA(@RequestParam("use2FA") final boolean use2FA)
			throws UnsupportedEncodingException {
		final User user = userService.updateUser2FA(use2FA);
		if (use2FA) {
			return new GenericResponse(userService.generateQRUrl(user));
		}
		return null;
	}

	// ============== NON-API ============

	private SimpleMailMessage constructGrantAuthorityVerificationTokenEmail(final String contextPath, final Locale locale,
			final VerificationToken newToken, final String fromName, final String emailTo) {
		final String grantFulfillUrl = contextPath + "/user/grantFulfill?token=" + newToken.getToken();
		final String message = messages.getMessage("message.grant.bestow", new String[] {fromName}, locale);
		return constructEmail("An invitation from Kutumbini on behalf of " + fromName + " to create/extend your family tree " + fromName, 
				message + " \r\n" + grantFulfillUrl, emailTo);
	}

	private SimpleMailMessage constructResendVerificationTokenEmail(final String contextPath, final Locale locale,
			final VerificationToken newToken, final User user) {
		final String confirmationUrl = contextPath + "/registrationConfirm.html?token=" + newToken.getToken();
		final String message = messages.getMessage("message.resendToken", null, locale);
		return constructEmail("Resend Registration Token", message + " \r\n" + confirmationUrl, user.getEmail());
	}

	private SimpleMailMessage constructResetTokenEmail(final String contextPath, final Locale locale,
			final String token, final User user) {
		final String url = contextPath + "/user/changePassword?id=" + user.getId() + "&token=" + token;
		final String message = messages.getMessage("message.resetPassword", null, locale);
		return constructEmail("Reset Password", message + " \r\n" + url, user.getEmail());
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
		return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
	}

	public void authWithHttpServletRequest(HttpServletRequest request, String username, String password) {
		try {
			request.login(username, password);
		} catch (ServletException e) {
			LOGGER.error("Error while login ", e);
		}
	}

	public void authWithAuthManager(HttpServletRequest request, String username, String password) {
		UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(username, password);
		authToken.setDetails(new WebAuthenticationDetails(request));
		Authentication authentication = authenticationManager.authenticate(authToken);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		// request.getSession().setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
		// SecurityContextHolder.getContext());
	}
	
	private User getUser() {
		AbstractAuthenticationToken auth = (AbstractAuthenticationToken) SecurityContextHolder.getContext()
				.getAuthentication();
		return (User) auth.getPrincipal();
	}

}

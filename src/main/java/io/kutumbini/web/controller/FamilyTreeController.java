package io.kutumbini.web.controller;

import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.services.FamilyTreeService;

// TODO ygiri add input validation and show appropriate error responses to user
@RestController
@RolesAllowed("ROLE_USER")
public class FamilyTreeController {

	@Autowired
	private FamilyTreeService familyTreeService;

	@GetMapping("/editTree")
	public String editTree(@RequestParam String firstname, @RequestParam String lastname, @RequestParam Long fromNodeID, @RequestParam String relation) {
		User user = getUser();
		familyTreeService.addPerson(user, firstname,lastname, fromNodeID, relation);
		return familyTreeService.userEditableTree(user, 1000);
	}

	@GetMapping("/userHomeTree")
	public String userHomeTree(@RequestParam(value = "limit", required = false) Integer limit) {
		return familyTreeService.userExtendedTree(getUser(), limit == null ? 1000 : limit);
	}

	@GetMapping("/userHomeD3Tree")
	public Map<String, Object> userHomeD3Tree(@RequestParam(value = "limit", required = false) Integer limit) {
		return familyTreeService.userExtendedTreeD3(getUser(), limit == null ? 1000 : limit);
	}

	@GetMapping("/publicHomeTree")
	public String publicHomeTree(@RequestParam(value = "limit", required = false) Integer limit) {
		return familyTreeService.publicTree(limit == null ? 1000 : limit);
	}

	@GetMapping("/publicHomeD3Tree")
	public Map<String, Object> publicHomeD3Tree(@RequestParam(value = "limit", required = false) Integer limit) {
		return familyTreeService.publicTreeD3(limit == null ? 1000 : limit);
	}
	
	private User getUser() {
		AbstractAuthenticationToken auth = (AbstractAuthenticationToken) SecurityContextHolder.getContext()
				.getAuthentication();
		return (User) auth.getPrincipal();
	}
}

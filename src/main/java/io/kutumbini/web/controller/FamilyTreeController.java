package io.kutumbini.web.controller;

import java.util.Map;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.domain.entity.Gender;
import io.kutumbini.services.FamilyTreeService;

// TODO ygiri add input validation and show appropriate error responses to user
@RestController
@RolesAllowed("ROLE_USER")
public class FamilyTreeController {

	@Autowired
	private FamilyTreeService familyTreeService;

	@GetMapping("/createFamily")
	public String createFamily(String husbandFirstname, String husbandLastname, String wifeFirstname, String wifeLastname) {
		User user = getUser();
		familyTreeService.createFamily(user, husbandFirstname, husbandLastname, wifeFirstname, wifeLastname);
		return "";
	}

	@GetMapping("/addChildById")
	public String addChildById(Long childNodeId, Long familyNodeId) {
		User user = getUser();	
		familyTreeService.addChild(childNodeId, familyNodeId);
		return "";
	}

	@GetMapping("/addChild")
	public String addChild(String firstname, String lastname, String gender, Long familyNodeId) {
		User user = getUser();	
		familyTreeService.addChild(user, firstname, lastname, Gender.valueOf(gender), familyNodeId);
		return "";
	}

	@GetMapping("/editFamily")
	public Map<String, Object> editFamilyData() {
		return familyTreeService.editFamilyData(getUser());
	}

	@GetMapping("/viewFamilyData")
	public Map<String, Object> viewFamilyData() {
		return familyTreeService.viewExtendedFamilyData(getUser());
	}

	@GetMapping("/viewExtendedFamilyData")
	public Map<String, Object> viewFamilyExtendedData() {
		return familyTreeService.viewExtendedFamilyData(getUser());
	}

	
	private User getUser() {
		AbstractAuthenticationToken auth = (AbstractAuthenticationToken) SecurityContextHolder.getContext()
				.getAuthentication();
		return (User) auth.getPrincipal();
	}
}

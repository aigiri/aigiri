package io.kutumbini.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.kutumbini.auth.persistence.model.User;
import io.kutumbini.domain.entity.Family;
import io.kutumbini.domain.entity.Person;
import io.kutumbini.services.FamilyTreeService;
import io.kutumbini.web.model.Constants;
import io.kutumbini.web.model.EditDataProcessor;
import io.kutumbini.web.model.FamilyCouncelor;

// TODO ygiri add input validation and show appropriate error responses to user
@RestController
@RolesAllowed("ROLE_USER")
public class FamilyTreeController {

	@Autowired
	private FamilyTreeService familyTreeService;

	@PostMapping("/createPerson")
	public String createPerson(HttpSession session) {
		Person p = familyTreeService.createPerson(getUser());
		setFamilyDataInSession(session);
		return p.getId().toString();
	}

	@PostMapping("/deletePerson")
	public String deletePerson(@RequestBody Long id, HttpSession session) {
		familyTreeService.deletePerson(id, getUser());
		setFamilyDataInSession(session);
		return "success";
	}

	@PostMapping("/deleteAll")
	public String deleteAll(HttpSession session) {
		familyTreeService.deleteFamilyTree(getUser());
		setFamilyDataInSession(session);
		return "success";
	}

	/**
	 * Sets the data in the session and resyncs on /saveFamilyData call
	 */
	@GetMapping("/editFamily")
	public ModelAndView editFamily(HttpSession session) {
		setFamilyDataInSession(session);
		return new ModelAndView("editFamily.html");
	}

	private void setFamilyDataInSession(HttpSession session) {
		List<Person> persons = familyTreeService.getEditablePersons(getUser());
		List<Family> families = familyTreeService.getEditableFamlies(getUser());
		session.setAttribute(Constants.PERSONS, persons);
		session.setAttribute(Constants.FAMILIES, families);
		List<Map<String, Object>> data = EditDataProcessor.getEditFamilyData(persons, families);
		session.setAttribute(Constants.EDIT_TABLE_DATA, data);
	}

	/**
	 * Resync with data in the session and then save to the repository
	 */
	@SuppressWarnings("unchecked")
	@PostMapping("/saveFamilyData")
	public String saveFamilyData(@RequestBody String familyData, HttpSession session) throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		List<Map<String, Object>> edited = mapper.readValue(familyData, List.class);
		FamilyCouncelor familyCouncelor = new FamilyCouncelor(session, getUser(), familyTreeService);
		EditDataProcessor editDataProcessor = new EditDataProcessor(familyCouncelor);
		editDataProcessor.updateSessionData(edited, getUser());
		familyCouncelor.saveToRepository();
		setFamilyDataInSession(session);
		return "success";
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

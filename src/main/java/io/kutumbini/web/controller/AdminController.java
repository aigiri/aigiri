package io.kutumbini.web.controller;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.kutumbini.services.AdminService;

@RestController
@RolesAllowed("ROLE_ADMIN")
public class AdminController {

	@Autowired
	private AdminService adminService;

	@GetMapping("/admin")
	public void admin() {
		System.out.println("In admin()");
	}

}

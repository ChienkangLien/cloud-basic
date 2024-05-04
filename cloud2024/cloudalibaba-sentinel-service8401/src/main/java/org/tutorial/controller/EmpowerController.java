package org.tutorial.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EmpowerController {
	@GetMapping("/empower")
	public String testEmpower() {
		System.out.println("授權規則");
		return "授權規則";
	}
}

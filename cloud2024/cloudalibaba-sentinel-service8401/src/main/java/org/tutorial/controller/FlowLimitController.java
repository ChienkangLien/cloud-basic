package org.tutorial.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FlowLimitController {
	
	@GetMapping("/testA")
	public String testA() {
		return "into ... A ...";
	}

	@GetMapping("/testB")
	public String testB() {
		return "into ... B ...";
	}
}

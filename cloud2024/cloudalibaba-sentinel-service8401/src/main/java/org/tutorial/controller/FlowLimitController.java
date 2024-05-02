package org.tutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.service.FlowLimitService;

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

	// 流控-鏈路驗證，C和D兩個請求都訪問flowLimitService.common()方法，閾值到達後對C限流，對D不管
	@Autowired
	private FlowLimitService flowLimitService;

	@GetMapping("/testC")
	public String testC() {
		flowLimitService.common();
		return "------testC";
	}

	@GetMapping("/testD")
	public String testD() {
		flowLimitService.common();
		return "------testD";
	}

	// 流控效果-排隊等待
	@GetMapping("/testE")
	public String testE() {
		System.out.println(System.currentTimeMillis() + "  testE，排隊等待");
		return "------testE";
	}
}

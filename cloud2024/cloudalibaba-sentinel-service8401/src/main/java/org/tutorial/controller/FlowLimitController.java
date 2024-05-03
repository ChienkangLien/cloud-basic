package org.tutorial.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.service.FlowLimitService;

@RestController
public class FlowLimitController {

	@GetMapping("/testA")
	public String testA() {
		return "------testA";
	}

	@GetMapping("/testB")
	public String testB() {
		return "------testB";
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

	// 熔斷策略-慢調用
	@GetMapping("/testF")
	public String testF() {
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		System.out.println("熔斷測試，慢調用比例");
		return "------testF";
	}

	// 熔斷策略-異常比例
	@GetMapping("/testG")
	public String testG() {
		throw new RuntimeException("模擬異常");
	}
}

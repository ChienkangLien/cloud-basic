package org.tutorial.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RefreshScope
public class PayAlibabaController {
	@Value("${server.port}")
	private String serverPort;

	@GetMapping(value = "/pay/nacos/{id}")
	public String getPayInfo(@PathVariable("id") Integer id) {
		return "nacos registry, serverPort: " + serverPort + ", id: " + id;
	}
	
	@Value("${config.info}")
	private String configInfo;

	@GetMapping("/config/info")
	public String getConfigInfo() {
		return "serverPort: " + serverPort + " " +configInfo;
	}
}

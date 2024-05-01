package org.tutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/consumer")
public class OrderNacosController {
	@Autowired
	private RestTemplate restTemplate;

	@Value("${service-url.nacos-user-service}")
	private String serverURL;

	@GetMapping("/pay/nacos/{id}")
	public String paymentInfo(@PathVariable("id") Integer id) {
		String result = restTemplate.getForObject(serverURL + "/pay/nacos/" + id, String.class);
		return result + "    我是OrderNacosController83調用者...";
	}
	
	@GetMapping("/config/info")
	public String paymentConfigInfo() {
		String result = restTemplate.getForObject(serverURL + "/config/info" , String.class);
		return result + "    我是OrderNacosController83調用者...";
	}
}

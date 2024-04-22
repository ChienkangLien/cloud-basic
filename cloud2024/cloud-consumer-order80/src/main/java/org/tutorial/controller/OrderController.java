package org.tutorial.controller;

import java.util.List;
import java.util.StringJoiner;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.tutorial.entities.PayDTO;
import org.tutorial.resp.ResultData;

@RestController
@RequestMapping("/consumer")
public class OrderController {

	// 尚未引入服務註冊中心，寫死
//	public static final String PAYMENTSRV_URL = "http://localhost:8001";

	// 使用服務註冊中心上的微服務名稱
	public static final String PAYMENTSRV_URL = "http://cloud-payment-service";

	@Autowired
	private RestTemplate restTemplate;

	@GetMapping("/pay/add")
	public ResultData addOrder(PayDTO payDTO) {
		return restTemplate.postForObject(PAYMENTSRV_URL + "/pay/add", payDTO, ResultData.class);
	}

	@GetMapping("/pay/del/{id}")
	public ResultData deleteOrder(@PathVariable("id") Integer id) {
		return restTemplate.exchange(PAYMENTSRV_URL + "/pay/del/" + id, HttpMethod.DELETE, null, ResultData.class)
				.getBody();
	}

	@GetMapping("/pay/update")
	public ResultData updateOrder(PayDTO payDTO) {
		return restTemplate
				.exchange(PAYMENTSRV_URL + "/pay/update", HttpMethod.PUT, new HttpEntity<>(payDTO), ResultData.class)
				.getBody();
	}

	@GetMapping("/pay/get/{id}")
	public ResultData getPayInfo(@PathVariable("id") Integer id) {
		return restTemplate.getForObject(PAYMENTSRV_URL + "/pay/get/" + id, ResultData.class, id);
	}

	@GetMapping("/pay/getAll")
	public ResultData getPayListInfo() {
		return restTemplate.getForObject(PAYMENTSRV_URL + "/pay/getAll", ResultData.class);
	}

	// 驗證LoadBalancer
	@GetMapping(value = "/get/info")
	public String getInfoByConsul() {
		return restTemplate.getForObject(PAYMENTSRV_URL + "/pay/get/info", String.class);
	}

	// 驗證動態獲取所有上線服務列表
	@Autowired
	private DiscoveryClient discoveryClient;

	@GetMapping("/discovery")
	public String discovery() {
		List<String> services = discoveryClient.getServices();
		for (String service : services) {
			System.out.println(service);
		}
		System.out.println("=================");
		List<ServiceInstance> instances = discoveryClient.getInstances("cloud-payment-service");
		for (ServiceInstance instance : instances) {
			StringJoiner joiner = new StringJoiner("\t");
			joiner.add(instance.getServiceId());
			joiner.add(instance.getHost());
			joiner.add(instance.getPort() + "");
			joiner.add(instance.getUri() + "");
			System.out.println(joiner);
		}
		return instances.get(0).getServiceId() + ":" + instances.get(0).getPort();
	}
}

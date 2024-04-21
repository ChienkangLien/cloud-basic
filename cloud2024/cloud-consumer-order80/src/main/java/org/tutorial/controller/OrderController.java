package org.tutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
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
}

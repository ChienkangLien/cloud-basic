package org.tutorial.service;

import org.springframework.stereotype.Service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;

@Service
public class FlowLimitService {

	// Sentinel中的一個注解，用於標識資源，以便進行流量控制、熔斷降級等操作
	@SentinelResource(value = "common")
	public void common() {
		System.out.println("------FlowLimitService come in");
	}
}

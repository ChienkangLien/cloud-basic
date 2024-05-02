package org.tutorial.service;

import org.springframework.stereotype.Service;

import com.alibaba.csp.sentinel.annotation.SentinelResource;

@Service
public class FlowLimitService {
	@SentinelResource(value = "common")
	public void common() {
		System.out.println("------FlowLimitService come in");
	}
}

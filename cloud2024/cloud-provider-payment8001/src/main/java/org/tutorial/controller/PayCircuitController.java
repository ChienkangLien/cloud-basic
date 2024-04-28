package org.tutorial.controller;

import java.util.concurrent.TimeUnit;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.resp.ResultData;

import cn.hutool.core.util.IdUtil;

@RestController
public class PayCircuitController {

	// CircuitBreaker 的例子
	@GetMapping("/pay/circuit/{id}")
	public ResultData<String> myCircuit(@PathVariable("id") Integer id) {
		if (id < 0) {
			throw new RuntimeException("Id不能為負 ... ");
		}
		if (id == 9999) {
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return ResultData.success("Hello, circuit! inputId: " + id + " \t" + IdUtil.simpleUUID());
	}

	// Bulkhead 的例子
	@GetMapping("/pay/bulkhead/{id}")
	public ResultData<String> myBulkhead(@PathVariable("id") Integer id) {
		if (id < 0) {
			throw new RuntimeException("Id不能為負 ... ");
		}
		if (id == 9999) {
			try {
				TimeUnit.SECONDS.sleep(5);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return ResultData.success("Hello, bulkhead! inputId: " + id + " \t" + IdUtil.simpleUUID());
	}

	// RateLimit 的例子
	@GetMapping("/pay/rateLimit/{id}")
	public ResultData<String> myRateLimit(@PathVariable("id") Integer id) {
		return ResultData.success("Hello, rateLimit! inputId: " + id + " \t" + IdUtil.simpleUUID());
	}
}

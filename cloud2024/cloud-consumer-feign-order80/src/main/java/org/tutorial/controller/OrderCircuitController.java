package org.tutorial.controller;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.apis.PayFeignApi;
import org.tutorial.resp.ResultData;

import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

@RestController
@RequestMapping("/feign")
public class OrderCircuitController {

	@Autowired
	private PayFeignApi payFeignApi;

	@GetMapping("/pay/circuit/get/{id}")
	@CircuitBreaker(name = "cloud-payment-service", fallbackMethod = "fallback4CircuitBreaker")
	public ResultData<String> getPayById4CircuitBreaker(@PathVariable("id") Integer id) {
		return payFeignApi.myCircuit(id);
	}

//    @GetMapping("/pay/bulkhead/get/{id}")
//    @Bulkhead(name = "cloud-payment-service", fallbackMethod = "fallback4BulkheadSemaphore", type = Bulkhead.Type.SEMAPHORE)
//    public ResultData<String> getPayById4Bulkhead(@PathVariable("id") Integer id) {
//    	return payFeignApi.myBulkhead(id);
//    }

	@GetMapping("/pay/bulkhead/get/{id}")
	@Bulkhead(name = "cloud-payment-service", fallbackMethod = "fallback4BulkheadThreadPool", type = Bulkhead.Type.THREADPOOL)
	public CompletableFuture<ResultData<String>> getPayById4Bulkhead(@PathVariable("id") Integer id) {
		System.out.println(Thread.currentThread().getName() + " into ...");
		try {
			TimeUnit.SECONDS.sleep(5);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		System.out.println(Thread.currentThread().getName() + " over ...");
		return CompletableFuture.supplyAsync(() -> payFeignApi.myBulkhead(id));
	}

	@GetMapping("/pay/rateLimit/get/{id}")
	@RateLimiter(name = "cloud-payment-service", fallbackMethod = "fallback4RateLimit")
	public ResultData<String> getPayById4RateLimit(@PathVariable("id") Integer id) {
		return payFeignApi.myRateLimit(id);
	}

	public ResultData<String> fallback4CircuitBreaker(Throwable throwable) {
		return ResultData.success("系統繁忙，請稍後重試...");
	}

	public ResultData<String> fallback4BulkheadSemaphore(Throwable throwable) {
		return ResultData.success("超出最大請求數量限制，請稍後重試...");
	}

	public CompletableFuture<ResultData<String>> fallback4BulkheadThreadPool(Throwable throwable) {
		return CompletableFuture.supplyAsync(() -> ResultData.success("超出最大請求數量限制，請稍後重試..."));
	}

	public ResultData<String> fallback4RateLimit(Throwable throwable) {
		return ResultData.success("服務器限流，請稍後重試...");
	}
}

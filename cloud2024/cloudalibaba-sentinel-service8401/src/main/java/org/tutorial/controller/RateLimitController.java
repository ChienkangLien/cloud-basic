package org.tutorial.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;

@RestController
public class RateLimitController {

	// 默認返回
	@GetMapping("/rateLimit/byUrl")
	public String byUrl() {
		return "按rest地址限流測試OK";
	}

	// 自定義返回
	@GetMapping("/rateLimit/byResource")
	@SentinelResource(value = "byResourceWithSentinelResource", blockHandler = "handleException")
	public String byResource() {
		return "按資源名稱byResourceWithSentinelResource限流測試OK";
	}

	public String handleException(BlockException exception) {
		return "服務不可用，@SentinelResource啟動";
	}

	// 自定義返回 + 服務降級處理
	@GetMapping("/rateLimit/doAction/{p1}")
	@SentinelResource(value = "doActionWithSentinelResource", blockHandler = "doActionBlockHandler", fallback = "doActionFallback")
	public String doAction(@PathVariable("p1") Integer p1) {
		if (p1 == 0) {
			throw new RuntimeException("p1等於0直接異常");
		}
		return "按資源名稱doActionWithSentinelResource限流測試OK";
	}

	public String doActionBlockHandler(@PathVariable("p1") Integer p1, BlockException e) {
		System.out.println("針對sentinel配置後出現的違規自定義返回：" + e);
		return "針對sentinel配置後出現的違規自定義返回";
	}

	public String doActionFallback(@PathVariable("p1") Integer p1, Throwable e) {
		System.out.println("JVM拋出的異常，服務降級：" + e);
		return "JVM拋出的異常，服務降級：" + e.getMessage();
	}
}
package org.tutorial.apis;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.tutorial.entities.PayDTO;
import org.tutorial.resp.ResultData;

@FeignClient(value = "nacos-payment-provider", fallback = PayFeignSentinelApiFallBack.class)
public interface PayFeignSentinelApi {

	@GetMapping("/pay/nacos/get/{orderNo}")
	public ResultData<PayDTO> getPayByOrderNo(@PathVariable("orderNo") String orderNo);
}

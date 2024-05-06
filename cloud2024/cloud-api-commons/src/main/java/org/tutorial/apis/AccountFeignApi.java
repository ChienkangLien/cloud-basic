package org.tutorial.apis;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tutorial.resp.ResultData;

@FeignClient(value = "seata-account-service")
public interface AccountFeignApi {

	// 扣餘額
	@PostMapping("/account/decrease")
    ResultData decrease(@RequestParam("userId") Long userId, @RequestParam("money") Long money);
}

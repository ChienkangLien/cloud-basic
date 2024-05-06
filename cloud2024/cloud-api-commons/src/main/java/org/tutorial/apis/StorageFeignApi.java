package org.tutorial.apis;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tutorial.resp.ResultData;

@FeignClient(value = "seata-storage-service")
public interface StorageFeignApi {
	
	// 減庫存
	@PostMapping("/storage/decrease")
	ResultData decrease(@RequestParam("productId") Long productId, @RequestParam("count") Integer count);
}

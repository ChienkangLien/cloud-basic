package org.tutorial.apis;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.tutorial.entities.PayDTO;
import org.tutorial.resp.ResultData;

//@FeignClient(value = "cloud-payment-service")
@FeignClient(value = "cloud-gateway") // for spring cloud gateway
public interface PayFeignApi {

	@PostMapping("/pay/add")
	ResultData<String> addPay(@RequestBody PayDTO pay);

	@DeleteMapping("/pay/del/{id}")
	ResultData<Integer> deletePay(@PathVariable("id") Integer id);

	@PutMapping("/pay/update")
	ResultData<String> updatePay(@RequestBody PayDTO payDTO);

	@GetMapping("/pay/get/{id}")
	ResultData<PayDTO> getById(@PathVariable("id") Integer id);

	@GetMapping("/pay/getAll")
	ResultData<List<PayDTO>> getAll();

 	// 驗證分布式配置
	@GetMapping("/pay/get/info")
	ResultData<String> getInfo();

	// 驗證Resilience4J 熔斷
	@GetMapping("/pay/circuit/{id}")
	ResultData<String> myCircuit(@PathVariable("id") Integer id);
	
	// 驗證Resilience4J 隔離
	@GetMapping("/pay/bulkhead/{id}")
	ResultData<String> myBulkhead(@PathVariable("id") Integer id);
	
	// 驗證Resilience4J 限流
	@GetMapping("/pay/rateLimit/{id}")
	ResultData<String> myRateLimit(@PathVariable("id") Integer id);
	
	// 驗證Micrometer
	@GetMapping("/pay/micrometer/{id}")
	ResultData<String> myMicrometer(@PathVariable("id") Integer id);
	
	// 驗證Gateway
    @GetMapping("/pay/gateway/get/{id}")
    ResultData<PayDTO> getById4Gateway(@PathVariable("id") Integer id);

    @GetMapping("/pay/gateway/info")
    ResultData<String> getInfo4Gateway();
}

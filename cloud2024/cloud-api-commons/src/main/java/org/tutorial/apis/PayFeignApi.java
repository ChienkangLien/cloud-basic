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

@FeignClient(value = "cloud-payment-service")
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

}

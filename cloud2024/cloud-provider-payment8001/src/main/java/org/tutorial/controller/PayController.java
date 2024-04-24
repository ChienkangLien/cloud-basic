package org.tutorial.controller;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.entities.Pay;
import org.tutorial.entities.PayDTO;
import org.tutorial.resp.ResultData;
import org.tutorial.service.PayService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pay")
@Tag(name = "支付微服務模塊", description = "訂單CRUD")
public class PayController {

	@Autowired
	private PayService payService;

	@PostMapping("/add")
	@Operation(summary = "新增", description = "新增支付流水, 參數是JSON字符串")
	public ResultData<String> addPay(@RequestBody Pay pay) {
		int i = payService.add(pay);
		return ResultData.success("成功新增資料，返回值為" + i);
	}

	@DeleteMapping("/del/{id}")
	@Operation(summary = "刪除", description = "刪除支付流水, 參數是Id")
	public ResultData<Integer> deletePay(@PathVariable("id") Integer id) {
		int i = payService.delete(id);
		return ResultData.success(i);
	}

	@PutMapping("/update")
	@Operation(summary = "更新", description = "更新支付流水, 參數是JSON字符串, 根據Id更新")
	public ResultData<String> updatePay(@RequestBody PayDTO payDTO) {
		Pay pay = new Pay();
		BeanUtils.copyProperties(payDTO, pay);
		int i = payService.update(pay);
		return ResultData.success("成功修改資料，返回值為" + i);
	}

	@GetMapping("/get/{id}")
	@Operation(summary = "查詢單個", description = "查詢支付流水, 參數是Id")
	public ResultData<Pay> getById(@PathVariable("id") Integer id) {

		// 驗證OpenFeign 超時控制，默認60秒，這裡睡62秒會拋出SocketTimeoutException：Read timed out
		if (id == 1) {
			System.out.println("調用8001 id 1");
			try {
				TimeUnit.SECONDS.sleep(62);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		Pay pay = payService.getById(id);
		return ResultData.success(pay);
	}

	@GetMapping("/getAll")
	@Operation(summary = "查詢所有", description = "查詢所有支付流水")
	public ResultData<List<Pay>> getAll() {
		List<Pay> list = payService.getAll();
		return ResultData.success(list);
	}

	// 驗證分布式配置
	@Value("${server.port}")
	private String port;

	@GetMapping(value = "/get/info")
	public ResultData<String> getInfoByConsul(@Value("${tutorial.info}") String tutorialInfo) {
		return ResultData.success("tutorialInfo: " + tutorialInfo + "\t" + "port: " + port);
	}
}

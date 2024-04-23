package org.tutorial.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.apis.PayFeignApi;
import org.tutorial.entities.PayDTO;
import org.tutorial.resp.ResultData;
import org.tutorial.resp.ReturnCodeEnum;

import cn.hutool.core.date.DateUtil;

@RestController
@RequestMapping("/feign")
public class OrderController {

	@Autowired
	private PayFeignApi payFeignApi;

	@GetMapping("/pay/add")
	public ResultData<String> addOrder(PayDTO payDTO) {
		return payFeignApi.addPay(payDTO);
	}

	@GetMapping("/pay/del/{id}")
	public ResultData<Integer> delOrder(@PathVariable("id") Integer id) {
		return payFeignApi.deletePay(id);
	}

	@GetMapping("/pay/update")
	public ResultData<String> updateOrder(PayDTO payDTO) {
		return payFeignApi.updatePay(payDTO);
	}

	@GetMapping("/pay/get/{id}")
	public ResultData<PayDTO> getPayById(@PathVariable("id") Integer id) {

		// 驗證OpenFeign 超時控制
		ResultData<PayDTO> resultData = null;
		try {
			System.out.println("調用開始----" + DateUtil.now());
			resultData = payFeignApi.getById(id);
		} catch (Exception e) {
			e.printStackTrace();
			resultData = ResultData.fail(ReturnCodeEnum.RC500.getCode(), e.getMessage());
		}
		System.out.println("調用結束----" + DateUtil.now());
		return resultData;
	}

	@GetMapping("/pay/getAll")
	public ResultData<List<PayDTO>> getAll() {
		return payFeignApi.getAll();
	}

	// 驗證LoadBalancer
	@GetMapping("/pay/get/info")
	public ResultData<String> getInfo() {
		return payFeignApi.getInfo();
	}
}

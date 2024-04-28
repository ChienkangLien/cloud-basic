package org.tutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.entities.Pay;
import org.tutorial.resp.ResultData;
import org.tutorial.service.PayService;

import cn.hutool.core.util.IdUtil;

@RestController
@RequestMapping("/pay/gateway")
public class PayGatewayController {
	@Autowired
	PayService payService;

	@GetMapping(value = "/get/{id}")
	public ResultData<Pay> getById(@PathVariable("id") Integer id) {
		Pay pay = payService.getById(id);
		return ResultData.success(pay);
	}

	@GetMapping(value = "/info")
	public ResultData<String> getGatewayInfo() {
		return ResultData.success("gateway info test：" + IdUtil.simpleUUID());
	}
}

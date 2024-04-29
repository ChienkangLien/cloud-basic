package org.tutorial.controller;

import java.util.Enumeration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.entities.Pay;
import org.tutorial.resp.ResultData;
import org.tutorial.service.PayService;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import jakarta.servlet.http.HttpServletRequest;

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

	@GetMapping(value = "/filter")
	public ResultData<String> getGatewayFilter(HttpServletRequest request) {
		String result = "";
		Enumeration<String> headers = request.getHeaderNames();
		while (headers.hasMoreElements()) {
			String headName = headers.nextElement();
			String headValue = request.getHeader(headName);
			System.out.println("請求頭名: " + headName + "\t\t\t" + "請求頭值: " + headValue);
			if (headName.equalsIgnoreCase("X-Request-test1") || headName.equalsIgnoreCase("X-Request-test2")) {
				result = result + headName + "\t " + headValue + " ";
			}
		}

		System.out.println("========================");
		System.out.println("request parameter customerId: " + request.getParameter("customerId"));
		System.out.println("request parameter customerName: " + request.getParameter("customerName"));
		System.out.println("========================");
		return ResultData.success("getGatewayFilter 過濾器 test： " + result + " \t " + DateUtil.now());
	}
}

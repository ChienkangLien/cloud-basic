package org.tutorial.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.entities.PayDTO;
import org.tutorial.resp.ResultData;
import org.tutorial.resp.ReturnCodeEnum;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;

import cn.hutool.core.util.IdUtil;

@RestController
@RefreshScope
public class PayAlibabaController {
	@Value("${server.port}")
	private String serverPort;

	@GetMapping(value = "/pay/nacos/{id}")
	public String getPayInfo(@PathVariable("id") Integer id) {
		return "nacos registry, serverPort: " + serverPort + ", id: " + id;
	}

	@Value("${config.info}")
	private String configInfo;

	@GetMapping("/config/info")
	public String getConfigInfo() {
		return "serverPort: " + serverPort + " " + configInfo;
	}

	// OpenFeign + Sentinel 集成
	// fallback處理，納入到Feign 接口統一處理
	@GetMapping("/pay/nacos/get/{orderNo}")
	@SentinelResource(value = "getPayByOrderNo", blockHandler = "handlerBlockHandler")
	public ResultData<PayDTO> getPayByOrderNo(@PathVariable("orderNo") String orderNo) {
		
		// 模擬從數據庫查詢出數據並賦值給DTO
		PayDTO payDTO = new PayDTO();
		payDTO.setId(1024);
		payDTO.setOrderNo(orderNo);
		payDTO.setAmount(BigDecimal.valueOf(9.9));
		payDTO.setPayNo("pay:" + IdUtil.fastUUID());
		payDTO.setUserId(1);

		return ResultData.success(payDTO);
	}

	public ResultData<String> handlerBlockHandler(@PathVariable("orderNo") String orderNo, BlockException exception) {
		return ResultData.fail(ReturnCodeEnum.RC500.getCode(),
				"getPayByOrderNo服務不可用，觸發sentinel流控配置規則");
	}
}

package org.tutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.apis.PayFeignApi;
import org.tutorial.entities.PayDTO;
import org.tutorial.resp.ResultData;

@RestController
@RequestMapping("/feign/gateway")
public class OrderGatewayController {
	
    @Autowired
    private PayFeignApi payFeignApi;


    @GetMapping("/pay/get/{id}")
    public ResultData<PayDTO> getPayById(@PathVariable("id") Integer id) {
        return payFeignApi.getById4Gateway(id);
    }

    @GetMapping("/pay/info")
    public ResultData<String> getInfo() {
        return payFeignApi.getInfo4Gateway();
    }

}

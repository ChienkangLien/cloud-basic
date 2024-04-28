package org.tutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.apis.PayFeignApi;
import org.tutorial.resp.ResultData;

@RestController
@RequestMapping("/feign")
public class OrderMicrometerController {

    @Autowired
    private PayFeignApi payFeignApi;

    @GetMapping("/pay/micrometer/get/{id}")
    public ResultData<String> myMicrometer(@PathVariable("id") Integer id) {
        return payFeignApi.myMicrometer(id);
    }

}

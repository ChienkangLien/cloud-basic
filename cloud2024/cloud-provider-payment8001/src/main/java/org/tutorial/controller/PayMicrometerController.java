package org.tutorial.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.resp.ResultData;

import cn.hutool.core.util.IdUtil;

@RestController
@RequestMapping("/pay")
public class PayMicrometerController {

    @GetMapping("/micrometer/{id}")
    public ResultData<String> myMicrometer(@PathVariable("id") Integer id) {
        return ResultData.success("這是鏈路追蹤, Id:" + IdUtil.simpleUUID());
    }
}

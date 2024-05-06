package org.tutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.resp.ResultData;
import org.tutorial.service.AccountService;

@RestController
public class AccountController {

	@Autowired
	private AccountService accountService;

	@PostMapping("/account/decrease")
	ResultData<String> decrease(@RequestParam("userId") Long userId, @RequestParam("money") Long money) {
		accountService.decrease(userId, money);
		return ResultData.success("扣減餘額成功");
	}
}

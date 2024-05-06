package org.tutorial.service.impl;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.repositories.AccountRepository;
import org.tutorial.service.AccountService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AccountServiceImpl implements AccountService {
	@Autowired
	private AccountRepository AccountRepository;

	@Override
	public void decrease(Long userId, Long money) {
		log.info("------------->AccountService 開始扣減餘額");
		AccountRepository.decrease(userId, money);
		log.info("------------->AccountService 開始扣減餘額");

		// 超時異常
//         timeout();
		// 拋出異常
//         int i = 10 / 0;
	}

	private void timeout() {
		try {
			TimeUnit.SECONDS.sleep(65);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

}

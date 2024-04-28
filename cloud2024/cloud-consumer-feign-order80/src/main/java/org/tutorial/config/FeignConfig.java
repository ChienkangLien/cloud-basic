package org.tutorial.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Logger;
import feign.Retryer;

@Configuration
public class FeignConfig {

	@Bean
	public Retryer myRetryer() {

		// 默認不重試
		return Retryer.NEVER_RETRY;

		// 初次間隔(ms) 最大間隔(s) 最大請求次數(1初次+2重試次數) = 3
//		return new Retryer.Default(100, 1, 3);
	}

	// 日誌記錄
	@Bean
	public Logger.Level feignLoggerLevel() {
		return Logger.Level.FULL;
	}

}

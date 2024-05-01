package org.tutorial;

import java.util.Arrays;
import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.Getter;
import lombok.Setter;
import reactor.core.publisher.Mono;

@Component
public class MyGatewayFilterFactory extends AbstractGatewayFilterFactory<MyGatewayFilterFactory.Config> {

	public MyGatewayFilterFactory() {
		super(MyGatewayFilterFactory.Config.class);
	}

	@Override
	public GatewayFilter apply(MyGatewayFilterFactory.Config config) {
		return new GatewayFilter() {
			@Override
			public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
				ServerHttpRequest request = exchange.getRequest();
				System.out.println("進入自定義過濾器MyGatewayFilterFactory，status: " + config.getStatus());

				if (request.getQueryParams().containsKey(config.getStatus())) {
					return chain.filter(exchange);
				}
				exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);
				return exchange.getResponse().setComplete();
			}
		};
	}

	@Override
	public List<String> shortcutFieldOrder() {
		return Arrays.asList("status");
	}

	public static class Config {
		@Getter
		@Setter
		private String status; // 設定一個狀態，匹配後才可訪問
	}

}

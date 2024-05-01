package org.tutorial;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MyGlobalFilter implements GlobalFilter, Ordered {

	private static final String BEGIN_VISIT_TIME = "begin_visit_time";

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		// 先記錄下訪問接口的開始時間
		exchange.getAttributes().put(BEGIN_VISIT_TIME, System.currentTimeMillis());

		return chain.filter(exchange).then(Mono.fromRunnable(() -> {
			Long beginVisitTime = exchange.getAttribute(BEGIN_VISIT_TIME);
			if (beginVisitTime != null) {
				log.info("訪問接口主機: " + exchange.getRequest().getURI().getHost());
				log.info("訪問接口端口: " + exchange.getRequest().getURI().getPort());
				log.info("訪問接口URL: " + exchange.getRequest().getURI().getPath());
				log.info("訪問接口URL參數: " + exchange.getRequest().getURI().getRawQuery());
				log.info("訪問接口時長: " + (System.currentTimeMillis() - beginVisitTime) + "ms");
				log.info("###################################################");
				System.out.println();
			}
		}));
	}

	@Override
	public int getOrder() {
		// 數字越小，優先級越高
		return 0;
	}
}

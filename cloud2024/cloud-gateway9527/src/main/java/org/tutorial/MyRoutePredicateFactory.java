package org.tutorial;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/*
 * 自定義配置會員等級userType，按照yml配置的會員等級，判斷可否訪問
 */
@Component
public class MyRoutePredicateFactory extends AbstractRoutePredicateFactory<MyRoutePredicateFactory.Config> {

	public MyRoutePredicateFactory() {
		super(MyRoutePredicateFactory.Config.class);
	}

	// 這個Config類就是路由斷言規則
	@Validated
	public static class Config {
		@Setter
		@Getter
		@NotEmpty
		private String userType; // 會員等級
	}

	@Override
	public Predicate<ServerWebExchange> apply(MyRoutePredicateFactory.Config config) {
		return new Predicate<ServerWebExchange>() {
			@Override
			public boolean test(ServerWebExchange serverWebExchange) {
				// 檢查request的參數里面，userType是否為指定的值，符合配置就通過
				String userType = serverWebExchange.getRequest().getQueryParams().getFirst("userType");

				if (userType == null)
					return false;

				// 如果說參數存在，就和config的數據進行比較
				if (userType.equalsIgnoreCase(config.getUserType())) {
					return true;
				}
				return false;
			}
		};
	}

	@Override
	public List<String> shortcutFieldOrder() {
	  return Collections.singletonList("userType");
	}
}

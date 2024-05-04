package org.tutorial.handler;

import org.springframework.stereotype.Component;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.RequestOriginParser;

import jakarta.servlet.http.HttpServletRequest;

@Component
public class MyRequestOriginParser implements RequestOriginParser {
	@Override
	public String parseOrigin(HttpServletRequest httpServletRequest) {
		return httpServletRequest.getParameter("serverName");
	}
}

package org.tutorial.exp;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.tutorial.resp.ResultData;
import org.tutorial.resp.ReturnCodeEnum;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public ResultData<String> doException(Exception e) {
		System.out.println("##### come in GlobalExceptionHandler");
		log.error("全局異常：{}", e.getMessage(), e);
		return ResultData.fail(ReturnCodeEnum.RC500.getCode(), e.getMessage());
	}
}

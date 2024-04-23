package org.tutorial.resp;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ResultData<T> {

	private String code;
	private String massage;
	private T data;
	private long timestamp;

	private ResultData() {
		this.timestamp = System.currentTimeMillis();
	}

	public static <T> ResultData<T> success(T data) {
		ResultData<T> result = new ResultData<>();
		result.setCode(ReturnCodeEnum.RC200.getCode());
		result.setMassage(ReturnCodeEnum.RC200.getMessage());
		result.setData(data);
		return result;
	}

	public static <T> ResultData<T> fail(String code, String massage) {
		ResultData<T> result = new ResultData<>();
		result.setCode(code);
		result.setMassage(massage);
		result.setData(null);
		return result;
	}

}

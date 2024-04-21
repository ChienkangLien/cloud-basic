package org.tutorial.resp;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum ReturnCodeEnum {

	// 1.舉值
	RC999("999", "操作失敗"),
	RC200("200", "Success"),
	RC201("201", "服務降級保護，請稍後再試"),
	RC202("202", "熱點參數限流，請稍後再試"),
	RC203("203", "系統規則不滿足要求，請稍後再試"),
	RC204("204", "授權規則不通過，請稍後再試"),
	
	RC400("400", "Bad Request"),
	RC401("401", "Unauthorized"),
	RC403("403", "Forbidden"),
	RC404("404", "Not Found"),
	RC500("500", "Internal Server Error"),
	
	RC375("2001", "訪問token不合法"),
	INVALID_TOKEN("2003", "沒有權限訪問此資源"),
	ACCESS_DENIED("1001", "客戶端認證失敗"),
	USERNAME_OR_PASSWORD_ERROR("1002", "用戶名或密碼錯誤"),
	BUSINESS_ERROR("1003", "業務邏輯異常"),
	UNSUPPORTED_GRANT_TYPE("1004", "不支持的認證模式");

	// 2.構造
	private final String code;
	private final String message;

	ReturnCodeEnum(String code, String message) {
		this.code = code;
		this.message = message;
	}

	// 3. 遍歷
	public static ReturnCodeEnum fromCodeV1(String code) {
		for (ReturnCodeEnum e : ReturnCodeEnum.values()) {
			if (e.getCode().equalsIgnoreCase(code)) {
				return e;
			}
		}
		return null;
	}

	public static ReturnCodeEnum fromCodeV2(String code) {
		return Arrays.stream(ReturnCodeEnum.values())
				.filter(e -> e.getCode().equalsIgnoreCase(code)).findFirst()
				.orElse(null);
	}
}

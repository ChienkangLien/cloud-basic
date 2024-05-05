package org.tutorial.apis;

import org.springframework.stereotype.Component;
import org.tutorial.entities.PayDTO;
import org.tutorial.resp.ResultData;
import org.tutorial.resp.ReturnCodeEnum;

@Component
public class PayFeignSentinelApiFallBack implements PayFeignSentinelApi {

	@Override
	public ResultData<PayDTO> getPayByOrderNo(String orderNo) {
		return ResultData.fail(ReturnCodeEnum.RC500.getCode(), "對方服務宕機或不可用，FallBack服務降級");
	}
}

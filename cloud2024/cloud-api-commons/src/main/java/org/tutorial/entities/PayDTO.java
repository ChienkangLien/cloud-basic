package org.tutorial.entities;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayDTO {

	private Integer id;

	private String payNo;

	private String orderNo;

	private Integer userId;

	private BigDecimal amount;

}

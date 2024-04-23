package org.tutorial.entities;

import java.math.BigDecimal;
import java.sql.Timestamp;

import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "t_pay")
@Data
@Schema(title = "支付交易表")
public class Pay {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Schema(title = "編號")
	private Integer id;

	@Schema(title = "支付流水號")
	private String payNo;

	@Schema(title = "訂單流水號")
	private String orderNo;

	@Schema(title = "用戶帳號ID")
	private Integer userId;

	@Schema(title = "交易金額")
	private BigDecimal amount;

	@Schema(title = "刪除標志, 默認0不刪除, 1刪除")
	private Integer deleted;

	@Schema(title = "創建時間")
	@Generated(event = EventType.INSERT)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Timestamp createTime;

	@Schema(title = "更新時間")
	@Generated(event = EventType.INSERT)
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
	private Timestamp updateTime;

	@PrePersist
	void preInsert() {
		if (this.userId == null)
			this.userId = 1;
		if (this.amount == null)
			this.amount = BigDecimal.valueOf(9.90);
		if (this.deleted == null)
			this.deleted = 0;
	}
}

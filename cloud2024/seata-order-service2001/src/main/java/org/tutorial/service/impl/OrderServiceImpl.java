package org.tutorial.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.tutorial.apis.AccountFeignApi;
import org.tutorial.apis.StorageFeignApi;
import org.tutorial.entities.Order;
import org.tutorial.repositories.OrderRepository;
import org.tutorial.service.OrderService;

import io.seata.core.context.RootContext;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
	@Autowired
	private OrderRepository orderRepository;
	@Autowired
	private StorageFeignApi storageFeignApi; // 訂單微服務通過OpenFeign調用庫存微服務
	@Autowired
	private AccountFeignApi accountFeignApi; // 訂單微服務通過OpenFeign調用帳戶微服務

	@Override
	public void create(Order order) {
		// xid全局事務id檢查
		String xid = RootContext.getXID();

		// 1. 新建訂單
		log.info("-------------> 開始新建訂單, XID: {}", xid);
		// 訂單新建時默認初始訂單狀態為零
		order.setStatus(0);
		Order orderFromDB = orderRepository.save(order);
		log.info("-------------> orderFromDB info：" + orderFromDB);

		if (orderFromDB.getId() > 0) {
			log.info("-------------> 新建訂單成功, OrderInfo: {}", orderFromDB);

			// 2. 扣減庫存
			log.info("-------------> 訂單微服務開始調用storage，扣減庫存");
			storageFeignApi.decrease(orderFromDB.getProductId(), orderFromDB.getCount());
			log.info("-------------> 訂單微服務結束調用storage，扣減庫存成功");

			// 3. 扣減帳戶餘額
			log.info("-------------> 訂單微服務開始調用account，扣減餘額");
			accountFeignApi.decrease(order.getUserId(), order.getMoney());
			log.info("-------------> 訂單微服務結束調用account，扣減餘額成功");

			// 4. 修改訂單狀態
			log.info("-------------> 開始修改訂單狀態");
			orderFromDB.setStatus(1);
			orderRepository.save(order);
			log.info("-------------> 修改訂單狀態成功");
			log.info("-------------> orderFromDB info：" + orderFromDB);
		}
		log.info("-------------> 結束新建訂單, XID: {}", xid);
	}

}

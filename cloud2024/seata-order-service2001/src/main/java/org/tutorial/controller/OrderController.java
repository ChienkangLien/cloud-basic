package org.tutorial.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tutorial.entities.Order;
import org.tutorial.resp.ResultData;
import org.tutorial.service.OrderService;

@RestController
public class OrderController {

	@Autowired
	private OrderService orderService;

	@GetMapping("/order/create")
	public ResultData<Order> create(Order order) {
		orderService.create(order);
		return ResultData.success(order);
	}
}

package com.jpl.smtm.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jpl.smtm.entity.Order;
import com.jpl.smtm.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
	
	//새로운 주문 접수
	@PostMapping("/create")
	public Order createOrder(@RequestBody Order order) {
		return orderService.createOrder(order);
	}
	
	//대기열 조회
	@GetMapping("/waiting")
	public List<Order> getWaitingQueue(){
		return orderService.getInitialQueue();
	}
	
	//주문 상태 변경
	@PatchMapping("/{orderNumber}/status")
	public Order updateStatus(@PathVariable Integer orderNumber, @RequestParam String status) throws Exception {
		return orderService.updateOrderStatus(orderNumber, status);
	}

}

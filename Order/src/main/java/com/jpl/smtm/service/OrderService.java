package com.jpl.smtm.service;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.jpl.smtm.constant.OrderStatus;
import com.jpl.smtm.constant.OrderType;
import com.jpl.smtm.entity.Order;
import com.jpl.smtm.repository.OrderRepository;

@Service
public class OrderService {
	
	@Autowired
	private OrderRepository orderRepository;
	
	@Autowired(required = false)
	private ChatService chatService;
	
	@Transactional
	public Order createOrder(Order order) {
		order.setOrderStatus(OrderStatus.WAITING);
		return orderRepository.save(order);
	}
	
	//내 주문 확인
	public Order getOrder(Integer orderNumber) {
		List<Order> orders = orderRepository.findByOrderNumber(orderNumber); 
		
		if (orders == null || orders.isEmpty()) {
			return null;
		}
		
		return orders.get(orders.size() - 1);
	 }
	
	//상태 변경
	@Transactional
	public Order updateOrderStatus(Integer orderNumber, String statusStr) throws Exception {
        OrderStatus status;
		try{
			status = OrderStatus.valueOf(statusStr);
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("유효하지 않음");
		}
		
		//리스트로 조회
		List<Order> orders = orderRepository.findByOrderNumber(orderNumber);
		if (orders == null || orders.isEmpty()) {
			throw new RuntimeException("주문 번호를 찾을 수 없음: " + orderNumber);
		}
		
		Order order = orders.get(orders.size() - 1); //최신 주문 선택
		
		if (order.getOrderType() == OrderType.STORE && status == OrderStatus.DELIVERING) {
			throw new IllegalArgumentException("매장 주문은 배달 불가");
		}

        order.setOrderStatus(status);
        Order savedOrder = orderRepository.save(order);

        // 대기열 조회
        List<Integer> waitingNumbers = orderRepository.findByOrderStatusInOrderByRegDateAsc(
                	Arrays.asList(OrderStatus.WAITING, OrderStatus.COMPLETED, OrderStatus.DELIVERING))
        			.stream().map(Order::getOrderNumber).collect(Collectors.toList());

        if (chatService != null) {
			chatService.notifyOrderUpdate(savedOrder, waitingNumbers);
		}

        return order;
    }
	
	public List<Order> getInitialQueue(){
		return orderRepository.findByOrderStatusInOrderByRegDateAsc(Arrays.asList(OrderStatus.WAITING, OrderStatus.COMPLETED, OrderStatus.DELIVERING));
	}
}

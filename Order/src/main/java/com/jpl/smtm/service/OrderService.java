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
	
	@Autowired
	private ChatService chatService;
	
	@Transactional
	public Order createOrder(Order order) {
		order.setOrderStatus(OrderStatus.WAITING);
		return orderRepository.save(order);
	}
	
	@Transactional
	public Order updateOrderStatus(Integer orderNumber, String statusStr) throws Exception {
        OrderStatus status;
		try{
			status = OrderStatus.valueOf(statusStr);
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("유효하지 않음");
		}
		
		Order order = orderRepository.findByOrderNumber(orderNumber)
				.orElseThrow(() -> new RuntimeException("주문없음"));

        //매장 주문은 "배달 중" 상태 없음
        if (order.getOrderType()  == OrderType.STORE && status == OrderStatus.DELIVERING) {
            throw new IllegalArgumentException("매장 주문은 '배달 중' 상태가 될 수 없습니다.");
        }

        order.setOrderStatus(status);
        orderRepository.save(order);

        // 대기열 조회
        List<Integer> waitingNumbers = orderRepository.findByOrderStatusInOrderByRegDateAsc(
                	Arrays.asList(OrderStatus.WAITING, OrderStatus.COMPLETED, OrderStatus.DELIVERING))
        			.stream().map(Order::getOrderNumber).collect(Collectors.toList());

        // ChatService를 통해 실시간 알림 발송
        chatService.notifyOrderUpdate(order, waitingNumbers);

        return order;
    }
	
	public List<Order> getInitialQueue(){
		return orderRepository.findByOrderStatusInOrderByRegDateAsc(Arrays.asList(OrderStatus.WAITING, OrderStatus.COMPLETED, OrderStatus.DELIVERING));
	}
}

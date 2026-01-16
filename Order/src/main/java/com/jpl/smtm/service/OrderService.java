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
	
	
	@Autowired(required = false) //주문 처리는 멈추지 않도록
	private ChatService chatService;
	
	//주문 생성
	@Transactional //DB 저장 도중 에러 발생 시 자동 롤백
	public Order createOrder(Order order) {
		order.setOrderStatus(OrderStatus.WAITING); //초기 단계는 항상 WAITING
		return orderRepository.save(order);
	}
	
	//내 주문 확인
	public Order getOrder(Integer orderNumber) {
		List<Order> orders = orderRepository.findByOrderNumber(orderNumber); 
		//orderNumber을 List로 받아옴
		if (orders == null || orders.isEmpty()) {
			return null;
		}
		
		return orders.get(orders.size() - 1); //가장 최근에 저장된 데이터 반환
	 }
	
	//주문 상태 변경
	@Transactional
	public Order updateOrderStatus(Integer orderNumber, String statusStr) throws Exception {
        OrderStatus status;
		try{
			//유효성 검사
			status = OrderStatus.valueOf(statusStr);
		} catch(IllegalArgumentException e) {
			throw new IllegalArgumentException("유효하지 않음");
		}
		
		//리스트로 주문 조회
		List<Order> orders = orderRepository.findByOrderNumber(orderNumber);
		if (orders == null || orders.isEmpty()) {
			throw new RuntimeException("주문 번호를 찾을 수 없음: " + orderNumber);
		}
		
		Order order = orders.get(orders.size() - 1); //최신 주문 선택
		
		if (order.getOrderType() == OrderType.STORE && status == OrderStatus.DELIVERING) {
			throw new IllegalArgumentException("매장 주문은 배달 불가");
		}
		
		//상태 변경 및 저장 (DB 반영)
        order.setOrderStatus(status);
        Order savedOrder = orderRepository.save(order);

        // 대기열 정보 갱신
        //WAITING, COMPLETED, DELIVERING 상태의 주문 합쳐서 리스트 생성
        List<Integer> waitingNumbers = orderRepository.findByOrderStatusInOrderByRegDateAsc(
                	Arrays.asList(OrderStatus.WAITING, OrderStatus.COMPLETED, OrderStatus.DELIVERING))
        			.stream().map(Order::getOrderNumber).collect(Collectors.toList());

        if (chatService != null) {
			chatService.notifyOrderUpdate(savedOrder, waitingNumbers);
		}

        return order;
    }
	
	//초기 접속 시 현재 대기열 현황 가져오기
	public List<Order> getInitialQueue(){
		return orderRepository.findByOrderStatusInOrderByRegDateAsc(Arrays.asList(OrderStatus.WAITING, OrderStatus.COMPLETED, OrderStatus.DELIVERING));
	}
}

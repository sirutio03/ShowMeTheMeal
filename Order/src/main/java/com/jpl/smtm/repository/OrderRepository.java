package com.jpl.smtm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jpl.smtm.constant.OrderStatus;
import com.jpl.smtm.entity.Order;


public interface OrderRepository extends JpaRepository<Order, Integer>{
	
	//주문번호 중복 시 서버 에러 발생하지 않도록 Optional -> List로 변경
	List<Order> findByOrderNumber(Integer orderNumber);
	//주문 시간 순서대로 오름차순 정렬하여 반환
	List<Order> findByOrderStatusInOrderByRegDateAsc(List<OrderStatus> statuses);

}

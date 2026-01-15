package com.jpl.smtm.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jpl.smtm.constant.OrderStatus;
import com.jpl.smtm.entity.Order;


public interface OrderRepository extends JpaRepository<Order, Integer>{
	
	List<Order> findByOrderNumber(Integer orderNumber);
	List<Order> findByOrderStatusInOrderByRegDateAsc(List<OrderStatus> statuses);

}

package com.jpl.smtm.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jpl.smtm.entity.Order;


public interface OrderRepository extends JpaRepository<Order, Integer>{
	
	List<Order> findByOrderStatusInOrderByRegDateAsc(List<String> statuses);
	Optional<Order> findByOrderNumber(Integer orderNumber);

}

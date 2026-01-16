package com.jpl.smtm.entity;

import java.time.LocalDateTime;

import com.jpl.smtm.constant.OrderStatus;
import com.jpl.smtm.constant.OrderType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "yourorder")
public class Order {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; //DB 내부 관리용
	
	@Column(name = "orderNumber")
	private Integer orderNumber;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "orderType")
	private OrderType orderType;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "orderStatus")
	private OrderStatus orderStatus;
	
	@Column(name = "regDate")
	private LocalDateTime regDate;

	public Order() {}
	
	public Order(Integer orderNumber, OrderType orderType, OrderStatus orderStatus) {
		this.orderNumber = orderNumber;
		this.orderType = orderType;
		this.orderStatus = orderStatus;
	}
	
	public void prePersist() {
		this.regDate = LocalDateTime.now();
	}

	public Integer getId() {
		return id;
	}

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public OrderType getOrderType() {
		return orderType;
	}

	public OrderStatus getOrderStatus() {
		return orderStatus;
	}

	public LocalDateTime getRegDate() {
		return regDate;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public void setOrderNumber(Integer orderNumber) {
		this.orderNumber = orderNumber;
	}

	public void setOrderType(OrderType orderType) {
		this.orderType = orderType;
	}

	public void setOrderStatus(OrderStatus orderStatus) {
		this.orderStatus = orderStatus;
	}

	public void setRegDate(LocalDateTime regDate) {
		this.regDate = regDate;
	}
	
	
	
	

}

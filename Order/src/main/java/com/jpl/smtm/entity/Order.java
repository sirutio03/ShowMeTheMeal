package com.jpl.smtm.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "yourorder")
public class Order {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
	
	@Column(name = "orderNumber")
	private Integer orderNumber;
	
	@Column(name = "orderType")
	private String orderType;
	
	@Column(name = "orderStatus")
	private String orderStatus;
	
	@Column(name = "reg_date")
	private LocalDateTime regDate;

	public Order() {}
	
	public Order(Integer orderNumber, String orderType, String orderStatus) {
		this.orderNumber = orderNumber;
		this.orderType = orderType;
		this.orderStatus = orderStatus;
	}

	public Integer getId() {
		return id;
	}

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public String getOrderType() {
		return orderType;
	}

	public String getOrderStatus() {
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

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public void setOrderStatus(String orderStatus) {
		this.orderStatus = orderStatus;
	}

	public void setRegDate(LocalDateTime regDate) {
		this.regDate = regDate;
	}
	
	
	
	

}

package com.jpl.smtm.dto;

import java.util.List;

import com.jpl.smtm.constant.OrderStatus;
import com.jpl.smtm.constant.OrderType;

public class ChatMessage {
	
	private String type;
	private Integer orderNumber;
	private OrderType orderType;
	private OrderStatus orderStatus;
	private List<Integer> waitingList;
	
	public ChatMessage() {}

	public ChatMessage(String type, Integer orderNumber, OrderType orderType, OrderStatus orderStatus, List<Integer> waitingList) {
		this.type = type;
		this.orderNumber = orderNumber;
		this.orderType = orderType;
		this.orderStatus = orderStatus;
		this.waitingList = waitingList;
	}

	public String getType() {
		return type;
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

	public List<Integer> getWaitingList() {
		return waitingList;
	}

	public void setType(String type) {
		this.type = type;
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

	public void setWaitingList(List<Integer> waitingList) {
		this.waitingList = waitingList;
	}

	
	
	
	

}

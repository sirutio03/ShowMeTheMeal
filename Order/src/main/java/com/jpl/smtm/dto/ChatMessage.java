package com.jpl.smtm.dto;

import java.util.List;

public class ChatMessage {
	
	private String type;
	private Integer orderNumber;
	private String orderType;
	private String message;
	private List<Integer> waitingList;
	
	public ChatMessage() {}

	public ChatMessage(String type, Integer orderNumber, String orderType, String message, List<Integer> waitingList) {
		this.type = type;
		this.orderNumber = orderNumber;
		this.orderType = orderType;
		this.message = message;
		this.waitingList = waitingList;
	}

	public ChatMessage(String type, String message) {
		this.type = type;
		this.message = message;
	}

	public String getType() {
		return type;
	}

	public Integer getOrderNumber() {
		return orderNumber;
	}

	public String getOrderType() {
		return orderType;
	}

	public String getMessage() {
		return message;
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

	public void setOrderType(String orderType) {
		this.orderType = orderType;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setWaitingList(List<Integer> waitingList) {
		this.waitingList = waitingList;
	}
	
	
	
	
	

}

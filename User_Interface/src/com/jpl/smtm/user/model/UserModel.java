package com.jpl.smtm.user.model;

import java.util.*;

public class UserModel {
    private List<String> orderList;
    
    // ★ 수정 1: 처음엔 주문 번호가 없습니다 (null)
    private String userOrderNo = null; 
    
    // ★ 수정 2: 상태도 초기화
    private String userOrderStatus = "주문 전"; 

    public UserModel() {
        this.orderList = new ArrayList<>();
    }

    public List<String> getOrderList() { return orderList; }
    
    public String getUserOrderNo() { return userOrderNo; }
    // ★ 중요: 컨트롤러가 주문 성공 후 이 값을 채워줄 수 있게 setter가 필요합니다.
    public void setUserOrderNo(String userOrderNo) { this.userOrderNo = userOrderNo; }

    public String getUserOrderStatus() { return userOrderStatus; }
    public void setUserOrderStatus(String status) { this.userOrderStatus = status; }
}
package com.jpl.smtm.user.model;

import java.util.*;

public class UserModel {
    private List<String> orderList;
    private String userOrderNo = "254"; // 테스트용 본인 번호
    private String userOrderStatus = "조리 중"; // 💡 초기 상태 설정

    public UserModel() {
        // 초기 대기열은 관리자로부터 LIST 신호를 받기 전까지 임시로 비워둠
        this.orderList = new ArrayList<>();
    }

    public List<String> getOrderList() { return orderList; }
    public String getUserOrderNo() { return userOrderNo; }
    public String getUserOrderStatus() { return userOrderStatus; }
    public void setUserOrderStatus(String status) { this.userOrderStatus = status; }
}
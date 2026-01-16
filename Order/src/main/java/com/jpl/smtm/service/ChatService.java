package com.jpl.smtm.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpl.smtm.dto.ChatMessage;
import com.jpl.smtm.entity.Order;
import com.jpl.smtm.handler.ChatHandler;

@Service
public class ChatService {
	
    @Autowired 
    private ChatHandler chatHandler;
    
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 주문 상태 변경 알림 전송 (가운데 UI + 왼쪽 UI 갱신용)
    //DTO 패킹
    public void notifyOrderUpdate(Order order, List<Integer> waitingList) throws Exception {
       ChatMessage response = new ChatMessage(
    		   "ORDER_UPDATE",
    		   order.getOrderNumber(),
    		   order.getOrderType(),
    		   order.getOrderStatus(),
    		   waitingList //갱신된 대기열 리스트
    		   );
        
       //Java Object -> JSON String 변환
        String json = objectMapper.writeValueAsString(response);
        //모든 세션에 전송 요청
        chatHandler.broadcastMessage(json);
    }

}
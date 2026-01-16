package com.jpl.smtm.handler;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class ChatHandler extends TextWebSocketHandler{
	
	//Set 사용으로 수정
	private final Set<WebSocketSession> sessions = Collections.synchronizedSet(new HashSet<>());
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
		System.out.println("새로운 연결: " + session.getId());
	}
	
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception{
		String payload = message.getPayload();
		System.out.println("받은 메세지: " + payload);
		broadcastMessage(payload);
	}
	
	//broadcastMessage를 handleTextMessage와 분리함
	public void broadcastMessage(String payload) throws Exception {
		for (WebSocketSession s : sessions) {
			if(s.isOpen()) {
				s.sendMessage(new TextMessage(payload));
			}
		}
	}
	
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
		sessions.remove(session);
		System.out.println("연결 해제: " + session.getId());
	}

}
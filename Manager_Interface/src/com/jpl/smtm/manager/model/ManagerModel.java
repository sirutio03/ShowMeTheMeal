package com.jpl.smtm.manager.model;

import java.util.*;

public class ManagerModel {
    // 💡 전체 순서를 유지하기 위한 리스트 (사용자 전광판 순서 고정용)
    private List<String> masterOrderList; 
    private List<String> cookingList; 
    private List<String> doneList;    
    private String chatRequestUser = "";

    public ManagerModel() {
        // 초기 데이터 (이미지 78a913 기준 순서)
        this.masterOrderList = new ArrayList<>();
        this.cookingList = new ArrayList<>(masterOrderList);
        this.doneList = new ArrayList<>(); // 초기 조리 완료는 비어있음
    }

    public List<String> getCookingList() { return cookingList; }
    public List<String> getDoneList() { return doneList; }
    
    // 💡 대기열 순서가 바뀌지 않도록 마스터 리스트를 문자열로 반환
    public String getMasterOrdersString() {
        return String.join(",", masterOrderList);
    }

    public void moveToDone(String no) {
        if (cookingList.remove(no)) {
            doneList.add(no);
        }
    }

    public void removeFromDone(String no) {
        doneList.remove(no);
        masterOrderList.remove(no); // 대기열 목록에서 영구 삭제
    }

    public void setChatRequestUser(String userNo) {
    	this.chatRequestUser = userNo;
    }
    public String getChatRequestUser() {
    	return chatRequestUser;
    }
}
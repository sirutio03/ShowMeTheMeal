package com.jpl.smtm.manager.controller;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap; // 동시성 문제 해결용
import javax.swing.*;
import javax.swing.Timer;

import com.jpl.smtm.manager.model.ManagerModel;
import com.jpl.smtm.manager.view.ManagerView;

public class ManagerController implements ActionListener, Runnable {
    private ManagerModel model;
    private ManagerView view;

    // 서버 주소
    private static final String API_URL = "http://localhost:8080/api/orders"; 

    // ★ [핵심 수정] 방금 완료 처리해서 잠시동안 서버 데이터를 무시할 번호들
    private Set<String> processingIds = ConcurrentHashMap.newKeySet();

    public ManagerController(ManagerModel model, ManagerView view) {
        this.model = model;
        this.view = view;

        this.view.getBtnChatNotify().addActionListener(this);
        this.view.getBtnPublicSend().addActionListener(this);

        new Thread(this::pollOrdersFromServer).start(); // 서버 감시 시작
        refreshUI();
        new Thread(this).start(); // 채팅 소켓 시작
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        String cmd = e.getActionCommand();

        // [채팅 로직]
        if (src == view.getBtnChatNotify()) {
            view.addChatTab(model.getChatRequestUser(), this);
        } else if (cmd != null && cmd.startsWith("SEND:")) {
            String userNo = cmd.split(":")[1];
            String msg = view.getAndClearInput(userNo);
            sendToUserSocket(userNo, "MSG|" + msg);
            view.appendCustomerMessage(userNo, "[관리자]: " + msg);
        } else if (cmd != null && cmd.startsWith("CLOSE:")) {
            view.removeChatTab(cmd.split(":")[1]);
            view.resetNotifyButton();
        } 
        
        // [★ 완료 버튼 로직 수정]
        else if (src instanceof JButton) {
            JButton btn = (JButton) src;
            if (btn.getText().contains("완료처리")) {
                String no = btn.getText().split(" ")[0]; // "101" 추출

                // 1. ★ [중요] "이 번호는 내가 처리했으니 당분간 서버말 듣지 마" 라고 등록
                processingIds.add(no);

                // 2. UI 이동
                model.moveToDone(no);
                refreshUI();

                // 3. 서버 DB 업데이트 요청
                updateStatusOnServer(no, "COMPLETED");

                // 4. 타이머 시작 (화면에서 사라지게 하기)
                startRemovalTimer(no);
            }
        }
    }

    // --- [서버 통신: 목록 가져오기] ---
    private void pollOrdersFromServer() {
        while (true) {
            try {
                URL url = new URL(API_URL + "/waiting"); 
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                
                if (conn.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = in.readLine()) != null) sb.append(line);
                    
                    parseJsonAndRefreshManager(sb.toString());
                }
                Thread.sleep(2000); 
            } catch (Exception e) { }
        }
    }

    private void parseJsonAndRefreshManager(String json) {
        String clean = json.replace("[", "").replace("]", "");
        if (clean.trim().isEmpty()) return;
        
        List<String> newCooking = new ArrayList<>();
        String[] items = clean.split("\\},\\{");
        
        for (String s : items) {
            String no = extract(s, "\"orderNumber\":");
            String st = extract(s, "\"orderStatus\":");
            
            // ★ [핵심 로직] 내가 방금 완료버튼 누른 애(processingIds)라면?
            // 서버가 아직 "COOKING"이라고 우겨도 무시하고 리스트에 넣지 않음!
            if (no != null && processingIds.contains(no)) {
                continue; 
            }

            if (st != null && (st.contains("WAITING") || st.contains("COOKING"))) {
                if (no != null) newCooking.add(no);
            }
        }

        // 모델 업데이트 및 UI 갱신
        model.getCookingList().clear();
        model.getCookingList().addAll(newCooking);
        SwingUtilities.invokeLater(this::refreshUI);
    }

    // --- [서버 통신: 상태 변경 요청] ---
    private void updateStatusOnServer(String orderNo, String status) {
        new Thread(() -> {
            try {
                // 한글/공백 인코딩
                String encodedStatus = URLEncoder.encode(status, "UTF-8");
                String urlString = API_URL + "/" + orderNo + "/status?status=" + encodedStatus;
                
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("PUT"); 
                conn.setRequestProperty("Content-Type", "application/json");

                int code = conn.getResponseCode();
                System.out.println("서버 업데이트 결과 (" + orderNo + "): " + code);
                
                if (code != 200) {
                    System.out.println("업데이트 실패! 서버 로그 확인 필요.");
                    // 실패하면 다시 살려내야 할 수도 있지만, 일단은 둠
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
    
    // --- [기타 유틸] ---
    private void refreshUI() {
        view.refreshQueues(model.getCookingList(), model.getDoneList());
        for (java.awt.Component c : view.getCookingButtons()) {
            if (c instanceof JButton) ((JButton)c).addActionListener(this);
        }
    }
    
    private void startRemovalTimer(final String no) {
        // 5초 뒤에 UI에서 완전히 삭제
        Timer t = new Timer(5000, e -> { 
            model.removeFromDone(no); 
            processingIds.remove(no); // ★ 이제 무시 목록에서 해제 (서버도 업데이트 됐을 테니)
            refreshUI(); 
        });
        t.setRepeats(false); 
        t.start();
    }
    
    private String extract(String s, String key) {
        int idx = s.indexOf(key); if(idx < 0) return null;
        int start = idx + key.length(); 
        int end = s.indexOf(",", start); if(end < 0) end = s.indexOf("}", start); if(end < 0) end = s.length();
        return s.substring(start, end).replace("\"", "").trim();
    }
    
    private void sendToUserSocket(String u, String d) { 
        new Thread(() -> { try(Socket s = new Socket("localhost", 6000)){
            PrintWriter o = new PrintWriter(s.getOutputStream(), true); o.println(d);
        }catch(Exception e){}}).start();
    }

    @Override public void run() { 
        try (ServerSocket s = new ServerSocket(5000)) {
            while(true) {
                try(Socket c = s.accept(); BufferedReader r = new BufferedReader(new InputStreamReader(c.getInputStream()))) {
                    String l = r.readLine(); if(l!=null) {
                        String[] split = l.split("\\|");
                        SwingUtilities.invokeLater(() -> {
                            view.updateNotifyButton(split[0]); model.setChatRequestUser(split[0]);
                            view.addChatTab(split[0], this);
                            view.appendCustomerMessage(split[0], "[고객]: " + split[1]);
                        });
                    }
                }
            }
        } catch(IOException e) {}
    }
}
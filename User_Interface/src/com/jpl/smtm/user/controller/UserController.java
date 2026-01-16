package com.jpl.smtm.user.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import com.jpl.smtm.user.model.UserModel;
import com.jpl.smtm.user.view.UserView;

public class UserController implements ActionListener, Runnable {
    private UserModel model;
    private UserView view;

    // ★ 서버 주소 (대기열 조회용)
    private static final String WAITING_URL = "http://localhost:8080/api/orders/waiting"; 
    
    // ★ [추가됨] 내 주문 상태만 따로 조회하는 주소 (뒤에 번호 붙여서 사용)
    private static final String CHECK_URL = "http://localhost:8080/api/orders/"; 
    
    // ★ 관리자 채팅 소켓 포트
    private static final int MANAGER_PORT = 5000; 

    public UserController(UserModel model, UserView view) {
        this.model = model;
        this.view = view;

        // 1. 이벤트 리스너 등록
        this.view.getBtnSend().addActionListener(this);
        this.view.getChatInput().addActionListener(this);

        // 2. 앱 실행 시 "주문 번호" 입력 팝업
        SwingUtilities.invokeLater(() -> {
            String myOrderNo = JOptionPane.showInputDialog(
                view, 
                "주문하신 번호를 입력해주세요:", 
                "주문 확인 로그인", 
                JOptionPane.QUESTION_MESSAGE
            );

            if (myOrderNo != null && !myOrderNo.trim().isEmpty()) {
                model.setUserOrderNo(myOrderNo.trim());
                view.updateStatusDisplay(myOrderNo, "연결 중...");
            } else {
                view.updateStatusDisplay("-", "번호 없음");
            }

            // 3. 서버 감시 시작
            new Thread(this).start(); 
        });
    }

    // [기능 1] 채팅 전송
    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = view.getInputText();
        
        if (msg != null && !msg.trim().isEmpty()) {
            view.appendMessage(msg, true);
            
            String myNo = model.getUserOrderNo();
            if (myNo == null) myNo = "Guest";
            sendChatToServer(myNo, msg);
            
            view.clearInput();
        }
    }

    // [기능 2] 서버 감시 (무한 반복)
    @Override
    public void run() {
        while (true) {
            try {
                // 1. 전체 대기열 목록 가져오기 (남들 상태 확인용)
                getWaitingListFromServer();
                
                // 2. ★ [추가됨] 내 주문 상태 별도로 확인하기 (중요!)
                // (내 번호가 있을 때만 실행)
                if (model.getUserOrderNo() != null) {
                    checkMyOrderStatus(model.getUserOrderNo());
                }
                
                Thread.sleep(1000); // 1초 대기
            } catch (InterruptedException e) {
                break;
            } catch (Exception e) {
                // 에러 무시
            }
        }
    }

    // ★ [추가됨] 내 주문 상태만 콕 집어서 가져오는 새 메서드
    private void checkMyOrderStatus(String myNo) {
    	new Thread(() -> { // UI 멈춤 방지를 위해 스레드 사용
            try {
                URL url = new URL(CHECK_URL + myNo);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                int code = conn.getResponseCode();
                // ★ 콘솔 로그 추가: 서버가 뭐라고 하는지 확인
                System.out.println("내 주문 조회(" + myNo + ") 응답 코드: " + code);

                if (code == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while((line = in.readLine()) != null) sb.append(line);
                    
                    String json = sb.toString();
                    System.out.println("서버에서 받은 데이터: " + json); // ★ 데이터 확인용 로그

                    // 데이터가 비어있으면 (DB에 번호가 없음)
                    if (json == null || json.trim().isEmpty()) {
                         SwingUtilities.invokeLater(() -> view.updateStatusDisplay(myNo, "주문 정보 없음"));
                         return;
                    }

                    String status = extractValue(json, "\"orderStatus\":");
                    
                    if (status != null) {
                        String korStatus = convertStatusToKorean(status);
                        if (!model.getUserOrderStatus().equals(korStatus)) {
                            model.setUserOrderStatus(korStatus);
                            SwingUtilities.invokeLater(() -> {
                                view.updateStatusDisplay(myNo, korStatus);
                            });
                        }
                    } else {
                        // JSON은 왔는데 status가 없는 경우
                         SwingUtilities.invokeLater(() -> view.updateStatusDisplay(myNo, "상태 알 수 없음"));
                    }
                } else if (code == 404 || code == 500) {
                     // 서버 에러나 못 찾은 경우
                     SwingUtilities.invokeLater(() -> view.updateStatusDisplay(myNo, "주문 번호 오류"));
                }
            } catch (Exception e) {
                System.out.println("통신 에러: " + e.getMessage());
                // e.printStackTrace(); 
            }
        }).start();
    }
    

    // 서버에서 대기열 목록 가져오기
    private void getWaitingListFromServer() {
        try {
            URL url = new URL(WAITING_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();
                parseJsonAndRefresh(content.toString());
            }
            conn.disconnect();
        } catch (Exception e) { }
    }

    // 대기열 JSON 파싱 및 리스트 갱신
    private void parseJsonAndRefresh(String json) {
        List<String> waitingNumbers = new ArrayList<>();
        String cleanJson = json.replace("[", "").replace("]", "");
        
        if (!cleanJson.trim().isEmpty()) {
            String[] orders = cleanJson.split("\\},\\{");
            
            for (String orderStr : orders) {
                String number = extractValue(orderStr, "\"orderNumber\":");
                String status = extractValue(orderStr, "\"orderStatus\":");
                
                // 대기 중이거나 조리 중인 것만 리스트(전광판)에 표시
                if (status != null && (status.contains("WAITING") || status.contains("COOKING"))) {
                    if (number != null) {
                        waitingNumbers.add(number);
                    }
                }
            }
        }
        
        SwingUtilities.invokeLater(() -> {
            view.refreshOrderList(waitingNumbers, model.getUserOrderNo());
        });
    }

    // 관리자에게 채팅 보내기
    private void sendChatToServer(String userNo, String message) {
        new Thread(() -> {
            try (Socket socket = new Socket("localhost", MANAGER_PORT)) {
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(userNo + "|" + message);
            } catch (Exception ex) { }
        }).start();
    }

    // JSON 값 추출 헬퍼
    private String extractValue(String source, String key) {
        int keyIndex = source.indexOf(key);
        if (keyIndex == -1) return null;
        int start = keyIndex + key.length();
        int end = source.indexOf(",", start);
        if (end == -1) end = source.indexOf("}", start);
        if (end == -1) end = source.length();
        String value = source.substring(start, end).trim();
        if (value.startsWith("\"")) value = value.substring(1);
        if (value.endsWith("\"")) value = value.substring(0, value.length() - 1);
        return value;
    }

    // 상태 한글 변환
    private String convertStatusToKorean(String status) {
        if (status == null) return "-";
        if (status.contains("WAITING")) return "대기 중";
        if (status.contains("COOKING")) return "조리 중";
        if (status.contains("COMPLETED")) return "조리 완료";
        if (status.contains("FINISHED")) return "수령 완료";
        if (status.contains("CANCELED")) return "취소됨";
        return status;
    }
}
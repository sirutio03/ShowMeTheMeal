package com.jpl.smtm.user.controller;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;
import com.jpl.smtm.user.model.UserModel;
import com.jpl.smtm.user.view.UserView;

public class UserController implements ActionListener, Runnable {
    private UserModel model;
    private UserView view;

    public UserController(UserModel model, UserView view) {
        this.model = model; this.view = view;
        this.view.getBtnSend().addActionListener(this);
        
        // 💡 실행 즉시 초기 화면 (254, 조리 중) 표시
        initView();
        
        new Thread(this).start();
    }

    private void initView() {
        view.updateStatusDisplay(model.getUserOrderNo(), model.getUserOrderStatus());
    }

 // UserController.java의 run() 메서드 내부 수신 로직
    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(6000)) {
            while (true) {
                try (Socket s = server.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                    String raw = in.readLine();
                    if (raw == null) continue;
                    
                    String[] data = raw.split("\\|");
                    String type = data[0];

                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            if (type.equals("LIST")) {
                                // 전광판 리스트 업데이트 (이건 모든 사용자가 동일하게 업데이트)
                                List<String> newList = Arrays.asList(data[1].split(","));
                                view.refreshOrderList(newList, model.getUserOrderNo());
                            } 
                            else if (type.equals("STATUS")) {
                                // 💡 여기서 필터링! 
                                String targetNo = data[1]; // 신호에 담긴 번호
                                String newStatus = data[2]; // "조리 완료"
                                
                                // 내 주문 번호와 일치할 때만 중앙 글자를 바꿈
                                if (targetNo.equals(model.getUserOrderNo())) {
                                    model.setUserOrderStatus(newStatus);
                                    view.updateStatusDisplay(model.getUserOrderNo(), newStatus);
                                }
                            } 
                            else if (type.equals("MSG") || type.equals("ADMIN")) {
                                view.appendMessage(data[1], false);
                            }
                        }
                    });
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String msg = view.getChatInput().getText();
        if (!msg.isEmpty()) {
            view.appendMessage(msg, true);
            sendToManager(model.getUserOrderNo() + "|" + msg);
            view.getChatInput().setText("");
        }
    }

    private void sendToManager(String data) {
        new Thread(new Runnable() {
            @Override public void run() {
                try (Socket s = new Socket("localhost", 5000)) {
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    out.println(data);
                } catch (Exception ex) { }
            }
        }).start();
    }
}
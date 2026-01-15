package com.jpl.smtm.manager.controller;

import java.awt.event.*;
import java.io.*;
import java.net.*;
import javax.swing.*;
import com.jpl.smtm.manager.model.ManagerModel;
import com.jpl.smtm.manager.view.ManagerView;

public class ManagerController implements ActionListener, Runnable {
    private ManagerModel model;
    private ManagerView view;

    public ManagerController(ManagerModel model, ManagerView view) {
        this.model = model; this.view = view;
        this.view.getBtnChatNotify().addActionListener(this);
        this.view.getBtnPublicSend().addActionListener(this);
        refreshUI();
        new Thread(this).start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        String cmd = e.getActionCommand();

        if (src == view.getBtnChatNotify()) {
            view.addChatTab(model.getChatRequestUser(), this);
        } else if (cmd != null && cmd.startsWith("SEND:")) {
            String userNo = cmd.split(":")[1];
            String msg = view.getAndClearInput(userNo);
            sendToUser(userNo, "MSG|" + msg);
            view.appendCustomerMessage(userNo, "[관리자]: " + msg);
        } else if (cmd != null && cmd.startsWith("CLOSE:")) {
            view.removeChatTab(cmd.split(":")[1]);
            view.resetNotifyButton();
        } // ManagerController.java의 actionPerformed 부분
        else if (src instanceof JButton) {
            JButton btn = (JButton) src;
            if (btn.getText().contains("완료처리")) {
                String no = btn.getText().split(" ")[0]; // 클릭한 버튼의 주문번호
                model.moveToDone(no);
                refreshUI();
                
                // 💡 중요: "STATUS|번호|조리 완료" 형태로 전송
                sendToUser(no, "STATUS|" + no + "|조리 완료"); 
                
                startRemovalTimer(no);
            }
        }
    }

    private void refreshUI() {
        view.refreshQueues(model.getCookingList(), model.getDoneList());
        for (java.awt.Component c : view.getCookingButtons()) {
            if (c instanceof JButton) ((JButton) c).addActionListener(this);
        }
        // 💡 모든 사용자에게 마스터 리스트 전송 (순서 고정)
        sendToUser("ALL", "LIST|" + model.getMasterOrdersString());
    }

    private void startRemovalTimer(final String no) {
        Timer timer = new Timer(5000, new ActionListener() {
            @Override public void actionPerformed(ActionEvent e) {
                model.removeFromDone(no);
                refreshUI(); // 리스트에서 삭제됨
                // 사용자의 중앙 상태는 '조리 완료' 그대로 유지해야 하므로 별도 STATUS 신호 안 보냄
            }
        });
        timer.setRepeats(false); timer.start();
    }

    private void sendToUser(String userNo, String data) {
        new Thread(new Runnable() {
            @Override public void run() {
                try (Socket s = new Socket("localhost", 6000)) {
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    out.println(data);
                } catch (Exception ex) { }
            }
        }).start();
    }

    @Override
    public void run() {
        try (ServerSocket server = new ServerSocket(5000)) {
            while (true) {
                try (Socket s = server.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()))) {
                    String raw = in.readLine();
                    if (raw == null) continue;
                    String[] d = raw.split("\\|");
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override public void run() {
                            view.updateNotifyButton(d[0]); model.setChatRequestUser(d[0]);
                            view.addChatTab(d[0], ManagerController.this);
                            view.appendCustomerMessage(d[0], "[고객]: " + d[1]);
                        }
                    });
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}
package com.jpl.smtm.manager.view;

import java.awt.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class ManagerView extends JFrame {
    private JPanel cookingContainer, doneContainer;
    private JButton btnChatNotify;
    private JTabbedPane chatTabbedPane; 
    
    // 전체 공지용 컴포넌트
    private JTextArea publicArea;
    private JTextField publicChatInput;
    private JButton btnPublicSend;

    // 고객별 채팅창 관리를 위한 Map
    private Map<String, JTextArea> chatAreas = new HashMap<>();
    private Map<String, JTextField> chatInputs = new HashMap<>();

    public ManagerView() {
        setTitle("밥주세요 - 관리자 화면");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(50, 50, 1200, 700);
        
        JPanel contentPane = new JPanel(new GridLayout(1, 4, 10, 0));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);

        // 1. WEST: 조리 중 대기열
        cookingContainer = new JPanel(new GridLayout(0, 1, 0, 5));
        contentPane.add(createQueuePanel("조리 중", cookingContainer));

        // 2. CENTER-LEFT: 조리 완료 대기열
        doneContainer = new JPanel(new GridLayout(0, 1, 0, 5));
        contentPane.add(createQueuePanel("조리 완료", doneContainer));

        // 3. CENTER-RIGHT: 채팅 알림 영역
        JPanel notifyPanel = new JPanel(new BorderLayout());
        notifyPanel.setBorder(new TitledBorder("채팅 알림"));
        btnChatNotify = new JButton("<html><center>채팅 요청 대기 중...</center></html>");
        btnChatNotify.setBackground(Color.BLACK);
        btnChatNotify.setForeground(Color.YELLOW);
        btnChatNotify.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        notifyPanel.add(btnChatNotify, BorderLayout.NORTH);
        contentPane.add(notifyPanel);

        // 4. EAST: 통합 채팅 영역 (공지 + 개별 탭)
        JPanel eastCombinedPanel = new JPanel(new GridLayout(2, 1, 0, 10));
        
        // 상단: 고정 공지창
        JPanel publicPanel = new JPanel(new BorderLayout());
        publicPanel.setBorder(new TitledBorder("전체 공지 (고정)"));
        publicArea = new JTextArea(); publicArea.setEditable(false);
        publicChatInput = new JTextField(); btnPublicSend = new JButton("공지 전송");
        JPanel pInput = new JPanel(new BorderLayout());
        pInput.add(publicChatInput, BorderLayout.CENTER); pInput.add(btnPublicSend, BorderLayout.EAST);
        publicPanel.add(new JScrollPane(publicArea), BorderLayout.CENTER);
        publicPanel.add(pInput, BorderLayout.SOUTH);

        // 하단: 고객별 채팅 탭
        chatTabbedPane = new JTabbedPane();
        chatTabbedPane.setBorder(new TitledBorder("고객별 채팅"));
        
        eastCombinedPanel.add(publicPanel);
        eastCombinedPanel.add(chatTabbedPane);
        contentPane.add(eastCombinedPanel);
    }

    // 알림 버튼 업데이트
    public void updateNotifyButton(String userNo) {
        btnChatNotify.setText("<html><center>💡 " + userNo + "번 고객의<br>채팅 요청!</center></html>");
    }

    public void resetNotifyButton() {
        btnChatNotify.setText("<html><center>채팅 요청 대기 중...</center></html>");
    }

    // 고객 채팅 탭 추가 (전송 및 상담 종료 버튼 포함)
    public void addChatTab(String userNo, java.awt.event.ActionListener listener) {
        String title = userNo + "번 고객";
        for (int i = 0; i < chatTabbedPane.getTabCount(); i++) {
            if (chatTabbedPane.getTitleAt(i).equals(title)) {
                chatTabbedPane.setSelectedIndex(i);
                return;
            }
        }
        JPanel pnl = new JPanel(new BorderLayout());
        JTextArea area = new JTextArea(); area.setEditable(false);
        JTextField input = new JTextField();
        
        JButton send = new JButton("전송");
        send.setActionCommand("SEND:" + userNo); send.addActionListener(listener);
        
        JButton close = new JButton("상담 종료");
        close.setActionCommand("CLOSE:" + userNo); close.addActionListener(listener);
        close.setBackground(new Color(255, 100, 100)); // 빨간색 버튼

        chatAreas.put(userNo, area); chatInputs.put(userNo, input);
        
        JPanel south = new JPanel(new BorderLayout());
        JPanel btnPanel = new JPanel(new GridLayout(1, 2));
        btnPanel.add(send); btnPanel.add(close);
        south.add(input, BorderLayout.CENTER); south.add(btnPanel, BorderLayout.EAST);
        
        pnl.add(new JScrollPane(area), BorderLayout.CENTER); 
        pnl.add(south, BorderLayout.SOUTH);
        
        chatTabbedPane.addTab(title, pnl);
        chatTabbedPane.setSelectedIndex(chatTabbedPane.getTabCount()-1);
    }

    public void removeChatTab(String userNo) {
        String title = userNo + "번 고객";
        for (int i = 0; i < chatTabbedPane.getTabCount(); i++) {
            if (chatTabbedPane.getTitleAt(i).equals(title)) {
                chatTabbedPane.remove(i);
                chatAreas.remove(userNo); chatInputs.remove(userNo);
                return;
            }
        }
    }

    public void appendCustomerMessage(String userNo, String msg) {
        if (chatAreas.containsKey(userNo)) chatAreas.get(userNo).append(msg + "\n");
    }

    public String getAndClearInput(String userNo) {
        String txt = chatInputs.get(userNo).getText();
        chatInputs.get(userNo).setText(""); return txt;
    }

    public void refreshQueues(List<String> cooking, List<String> done) {
        cookingContainer.removeAll();
        for (String no : cooking) {
            JButton btn = new JButton(no + " (완료처리)");
            cookingContainer.add(btn);
        }
        doneContainer.removeAll();
        for (String no : done) {
            JLabel lbl = new JLabel(no, SwingConstants.CENTER);
            lbl.setOpaque(true); lbl.setBackground(Color.WHITE);
            lbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            doneContainer.add(lbl);
        }
        revalidate(); repaint();
    }

    private JPanel createQueuePanel(String title, JPanel container) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(new TitledBorder(title));
        p.add(new JScrollPane(container), BorderLayout.CENTER);
        return p;
    }

    public JButton getBtnChatNotify() { return btnChatNotify; }
    public JButton getBtnPublicSend() { return btnPublicSend; }
    public JTextArea getPublicArea() { return publicArea; }
    public String getPublicInput() { return publicChatInput.getText(); }
    public void clearPublicInput() { publicChatInput.setText(""); }
    public Component[] getCookingButtons() { return cookingContainer.getComponents(); }
}
package com.jpl.smtm.user.view;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class UserView extends JFrame {
    private JPanel westPanel;
    private JLabel userOrderNo, userOrderStatus;
    private JPanel chatContainer;
    private JTextField chatInput;
    private JButton btnSend;

    public UserView() {
        setTitle("밥주세요 - 사용자 화면");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setBounds(100, 100, 900, 500);
        
        JPanel contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        /* 1. WEST 패널: 대기열 표시 영역 (전광판) */
        westPanel = new JPanel(new GridLayout(0, 1, 0, 5));
        westPanel.setBounds(10, 10, 200, 445);
        // 컨텐트 팬 - WEST
        contentPane.add(westPanel);

        /* 2. CENTER 패널: 현재 내 주문 상세 정보 */
        JPanel centerPanel = new JPanel(new GridLayout(2, 1));
        centerPanel.setBounds(225, 10, 388, 445);
        centerPanel.setBorder(new javax.swing.border.LineBorder(Color.LIGHT_GRAY, 1, true));
        
        // 주문 번호 표시 영역
        JPanel pnlNo = new JPanel(null);
        userOrderNo = new JLabel("", SwingConstants.CENTER); // 가운데 정렬
        userOrderNo.setFont(new Font("맑은 고딕", Font.BOLD, 50));
        userOrderNo.setBounds(120, 80, 150, 68);
        pnlNo.add(userOrderNo);

        // 주문 상태 표시 영역
        JPanel pnlStatus = new JPanel(null);
        userOrderStatus = new JLabel("", SwingConstants.CENTER); // 가운데 정렬
        userOrderStatus.setFont(new Font("맑은 고딕", Font.BOLD, 40));
        userOrderStatus.setBounds(95, 80, 200, 54);
        pnlStatus.add(userOrderStatus);
       
        centerPanel.add(pnlNo); 
        centerPanel.add(pnlStatus);
        // 컨텐트 팬 - WEST + CENTER
        contentPane.add(centerPanel);

        /* 3. EAST 패널: 채팅 화면 */
        JPanel eastPanel = new JPanel(new BorderLayout());
        eastPanel.setBounds(628, 10, 250, 445);
        eastPanel.setBackground(Color.LIGHT_GRAY);
        // 채팅 컨테이너
        chatContainer = new JPanel();
        chatContainer.setLayout(new BoxLayout(chatContainer, BoxLayout.Y_AXIS));
        chatContainer.setBackground(Color.LIGHT_GRAY);
        eastPanel.add(new JScrollPane(chatContainer), BorderLayout.CENTER);
        // 인풋 패널 (입력창, 전송 버튼)
        JPanel inputPanel = new JPanel(new BorderLayout());
        chatInput = new JTextField();
        btnSend = new JButton("전송");
        inputPanel.add(chatInput, BorderLayout.CENTER);
        inputPanel.add(btnSend, BorderLayout.EAST);
        eastPanel.add(inputPanel, BorderLayout.SOUTH);
        // 컨텐트 팬 - WEST + CENTER + EAST
        contentPane.add(eastPanel);
    }

    /* 대기열 리스트 갱신 */
    public void refreshOrderList(List<String> orders, String userOrderNo) {
        westPanel.removeAll(); // 이전 요소들 지우기
        
        for (String no : orders) {
            JLabel lbl = new JLabel(no, SwingConstants.CENTER);
            lbl.setFont(new Font("맑은 고딕", Font.BOLD, 18));
            lbl.setOpaque(true); // JLabel에서의 배경색 설정은 setOpaque
            
            // 사용자 본인의 주문 번호인 경우 하이라이트 처리
            if (no.equals(userOrderNo)) {
                lbl.setBackground(Color.DARK_GRAY);
                lbl.setForeground(Color.WHITE);
            } else {
                lbl.setBackground(Color.WHITE);
                lbl.setForeground(Color.BLACK);
            }
            // 주문 번호 간 경계선 그리기
            lbl.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
            westPanel.add(lbl);
        }
        
        // 업데이트
        westPanel.revalidate();
        westPanel.repaint();
    }

    /* 현재 주문 정보(번호, 상태) 업데이트 */
    public void updateStatusDisplay(String no, String status) {
        userOrderNo.setText(no);
        userOrderStatus.setText(status);
    }

    /* 채팅 메시지 추가 */
    public void appendMessage(String text, boolean isMe) {
        JPanel bubble = new JPanel(new java.awt.FlowLayout(isMe ? java.awt.FlowLayout.RIGHT : java.awt.FlowLayout.LEFT));
        bubble.setOpaque(false);
        JLabel lbl = new JLabel("<html><body style='width:120px'>" + text + "</body></html>");
        lbl.setOpaque(true);
        // 내 메시지: 검정배경/흰글씨, 상대방: 흰배경/검정글씨
        if (isMe) {
            lbl.setBackground(Color.BLACK); lbl.setForeground(Color.WHITE);
        } else {
            lbl.setBackground(Color.WHITE); lbl.setForeground(Color.BLACK);
        }
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        bubble.add(lbl);
        chatContainer.add(bubble); chatContainer.add(Box.createVerticalStrut(10));
        chatContainer.revalidate(); chatContainer.repaint();
    }

    // Controller가 전송 버튼 클릭 이벤트를 인식하고, 입력값을 가져오기 위한 Getter
    public JButton getBtnSend() { return btnSend; }
    public JTextField getChatInput() { return chatInput; }
}
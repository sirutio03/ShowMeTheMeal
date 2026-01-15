package com.jpl.smtm.user.view;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class MessageBubble extends JPanel {
    public MessageBubble(String text, boolean isMe) {
        setLayout(new FlowLayout(isMe ? FlowLayout.RIGHT : FlowLayout.LEFT));
        setOpaque(false);

        JLabel label = new JLabel("<html><body style='width: 120px'>" + text + "</body></html>");
        label.setOpaque(true);
        
        // 내 메시지: 검은색 바탕 + 흰색 글씨
        // 상대방 메시지: 흰색 바탕 + 검은색 글씨
        if (isMe) {
            label.setBackground(Color.BLACK);
            label.setForeground(Color.WHITE);
        } else {
            label.setBackground(Color.WHITE);
            label.setForeground(Color.BLACK);
        }

        label.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        
        add(label);
    }
}

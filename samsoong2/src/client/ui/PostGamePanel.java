package client.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class PostGamePanel extends JPanel {

    private final JLabel noticeLabel;
    private final JButton restartButton;
    private final JButton endButton;

    public PostGamePanel() {
        setLayout(new BorderLayout(10, 20));

        noticeLabel = new JLabel(
                "모든 라운드가 종료되었습니다.",
                SwingConstants.CENTER
        );
        noticeLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        restartButton = new JButton("게임 다시 시작");
        endButton = new JButton("게임 종료");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(restartButton);
        buttonPanel.add(endButton);

        add(noticeLabel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 기본적으로 버튼은 숨김
        setHostMode(false);
    }

    public void setHostMode(boolean isHost) {
        restartButton.setVisible(isHost);
        endButton.setVisible(isHost);
    }

    public void setNotice(String notice) {
        noticeLabel.setText(notice);
    }

    public void setRestartListener(Runnable listener) {
        restartButton.addActionListener(e -> listener.run());
    }

    public void setEndListener(Runnable listener) {
        endButton.addActionListener(e -> listener.run());
    }
}
package client.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class WaitingRoomPanel extends JPanel {

    private final JLabel roomInfoLabel;
    private final JPanel centerPanel;
    private final JButton leaveRoomButton;

    private boolean isHost = false;
    private Consumer<String> categorySelectListener;

    public WaitingRoomPanel() {
        setLayout(new BorderLayout(10, 10));

        roomInfoLabel = new JLabel("방 코드: ---- | 인원: 0/3", SwingConstants.CENTER);
        centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 20));
        leaveRoomButton = new JButton("방 나가기");

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottomPanel.add(leaveRoomButton);

        add(roomInfoLabel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // 방장 여부
    public void setHost(boolean isHost) {
        this.isHost = isHost;
    }

    public boolean isHost() {
        return isHost;
    }

    // ROOM_UPDATED
    public void updateRoomStatus(String roomCode, int playerCount) {
        roomInfoLabel.setText("방 코드: " + roomCode + " | 인원: " + playerCount + "/3");

        centerPanel.removeAll();

        if (playerCount < 3) {
            centerPanel.add(new JLabel(
                "플레이어들을 기다리는 중입니다... (" + playerCount + "/3)",
                SwingConstants.CENTER
            ));
        } else {
            if (!isHost) {
                centerPanel.add(new JLabel("방장이 카테고리를 선택하는 중입니다...", SwingConstants.CENTER));
            }
        }

        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // 방장 화면
    public void showCategoryButtons(List<String> categories) {
        if (!isHost) {
            return;
        }

        centerPanel.removeAll();

        for (String category : categories) {
            JButton categoryButton = new JButton(category);
            categoryButton.addActionListener(e -> {
                if (categorySelectListener != null) {
                    categorySelectListener.accept(category);
                }
            });
            centerPanel.add(categoryButton);
        }

        centerPanel.revalidate();
        centerPanel.repaint();
    }

    // 카테고리 버튼 클릭
    public void setCategorySelectListener(Consumer<String> listener) {
        this.categorySelectListener = listener;
    }

    public JButton getLeaveRoomButton() {
        return leaveRoomButton;
    }
}
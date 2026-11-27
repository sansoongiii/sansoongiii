package client.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class RoomEntryPanel extends JPanel {

    private final JLabel titleLabel;
    private final JButton createRoomButton;
    private final JButton joinRoomButton;

    public RoomEntryPanel() {
        setLayout(new BorderLayout(10, 10));

        titleLabel = new JLabel("삼숭이 게임", SwingConstants.CENTER);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 28f));

        createRoomButton = new JButton("방 생성");
        joinRoomButton = new JButton("방 입장");

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.add(createRoomButton);
        buttonPanel.add(joinRoomButton);

        add(titleLabel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    public JButton getCreateRoomButton() {
        return createRoomButton;
    }

    public JButton getJoinRoomButton() {
        return joinRoomButton;
    }

    public void setButtonsEnabled(boolean enabled) {
        createRoomButton.setEnabled(enabled);
        joinRoomButton.setEnabled(enabled);
    }
}
package client.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class DrawingPanel extends JPanel {

    private final JLabel descriptionLabel;
    private final CanvasPanel canvasPanel;
    private final JButton penButton;
    private final JButton eraserButton;
    private final JButton clearButton;
    private final JButton submitButton;

    public DrawingPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(new java.awt.Color(229, 231, 235));

        descriptionLabel = new JLabel("", SwingConstants.CENTER);
        canvasPanel = new CanvasPanel();

        penButton = new JButton("펜");
        eraserButton = new JButton("지우개");
        clearButton = new JButton("전체 지우기");
        submitButton = new JButton("그림 제출");

        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(penButton);
        buttonPanel.add(eraserButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(submitButton);
        
        penButton.addActionListener(e -> {
            canvasPanel.usePen();
        });
        eraserButton.addActionListener(e -> {
            canvasPanel.useEraser();
        });
        clearButton.addActionListener(e -> {
            canvasPanel.requestClearAll();
        });

        add(descriptionLabel, BorderLayout.NORTH);
        add(canvasPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        setCanDraw(false);
    }

    // 서버에서 전달받은 설명 표시
    public void setDescription(String description) {
        descriptionLabel.setText(description);
    }

    // 그림 데이터 가져오기
    public byte[] getDrawingData() throws IOException {
        return canvasPanel.exportPng();
    }

    // 그림판 초기화
    public void clearCanvas() {
        canvasPanel.requestClearAll();
        submitButton.setEnabled(true);
    }

    // 그리기 활성화 및 비활성화
    public void setCanDraw(boolean canDraw) {
        canvasPanel.setCanDraw(canDraw);
        
        penButton.setEnabled(canDraw);
        eraserButton.setEnabled(canDraw);
        clearButton.setEnabled(canDraw);
        submitButton.setEnabled(canDraw);
    }

    public JButton getSubmitButton() {
        return submitButton;
    }
    
    public void setProgressListener(Consumer<byte[]> listener) {
        canvasPanel.setProgressListener(listener);
    }
}
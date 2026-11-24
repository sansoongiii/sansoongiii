package client.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AnswererPanel extends JPanel {

    private final JLabel drawingLabel;
    private final JTextField answerField;
    private final JButton submitButton;
    private final Timer saveTimer;

    private Consumer<String> progressListener;

    public AnswererPanel() {
        setLayout(new BorderLayout(10, 10));

        drawingLabel = new JLabel("그림을 기다리는 중입니다.", SwingConstants.CENTER);
        answerField = new JTextField(20);
        submitButton = new JButton("정답 제출");

        saveTimer = new Timer(500, e -> saveProgress());
        saveTimer.setRepeats(false);

        answerField.getDocument().addDocumentListener(
            new DocumentListener() {
                @Override
                public void insertUpdate(DocumentEvent e) {
                    scheduleSave();
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    scheduleSave();
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    scheduleSave();
                }
            }
        );
        
        JPanel inputPanel = new JPanel(new FlowLayout());
        inputPanel.add(new JLabel("정답:"));
        inputPanel.add(answerField);
        inputPanel.add(submitButton);

        add(drawingLabel, BorderLayout.CENTER);
        add(inputPanel, BorderLayout.SOUTH);
    }

    private void scheduleSave() {
        saveTimer.restart();
    }

    private void saveProgress() {
        if (progressListener != null && answerField.isEnabled()) {
            progressListener.accept(answerField.getText());
        }
    }
    
    public void setProgressListener(Consumer<String> listener) {
        this.progressListener = listener;
    }
    
    // 서버에서 전달받은 그림 표시
    public void setDrawing(byte[] drawingData) {
    	if (drawingData == null || drawingData.length == 0) {
            drawingLabel.setIcon(null);
            drawingLabel.setText("Drawer가 그림을 그리지 않았습니다.");
            return;
        }
        try {
            BufferedImage image = ImageIO.read(
                new ByteArrayInputStream(drawingData)
            );

            if (image == null) {
            	drawingLabel.setIcon(null);
                drawingLabel.setText("그림을 표시할 수 없습니다.");
                return;
            }

            drawingLabel.setIcon(new ImageIcon(image));
            drawingLabel.setText("");

        } catch (IOException e) {
        	drawingLabel.setIcon(null);
            drawingLabel.setText("그림을 불러오지 못했습니다.");
        }
    }

    // 입력한 정답 가져오기
    public String getAnswer() {
        return answerField.getText().trim();
    }

    // 입력창 초기화
    public void clearInput() {
    	answerField.setText("");
        setInputEnabled(true);
    }

    // 제출 버튼 가져오기
    public JButton getSubmitButton() {
        return submitButton;
    }
    
    public void setInputEnabled(boolean enabled) {
        answerField.setEnabled(enabled);
        submitButton.setEnabled(enabled);
    }
}
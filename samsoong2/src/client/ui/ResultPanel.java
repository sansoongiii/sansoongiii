package client.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;

public class ResultPanel extends JPanel {

    private final JLabel resultLabel;
    private final JLabel answerLabel;
    private final JTextArea descriptionArea;
    private final JLabel drawingLabel;
    private final JLabel submittedAnswerLabel;
    private final JLabel continueStatusLabel;
    private final JButton nextButton;
    
    private BufferedImage originalDrawing;

    public ResultPanel() {
        setLayout(new BorderLayout(10, 10));

        resultLabel = new JLabel("", SwingConstants.CENTER);
        answerLabel = new JLabel("", SwingConstants.CENTER);
        descriptionArea = new JTextArea();
        drawingLabel = new JLabel("", SwingConstants.CENTER);
        submittedAnswerLabel = new JLabel("", SwingConstants.CENTER);
        continueStatusLabel = new JLabel(
                "다음 라운드 진행 확인: 0/3",
                SwingConstants.CENTER
        );
        nextButton = new JButton("다음 라운드");

        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        JPanel topPanel = new JPanel(new GridLayout(1, 2, 10, 10));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.add(new JLabel("작성한 설명"), BorderLayout.NORTH);
        textPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        JPanel imagePanel = new JPanel(new BorderLayout());
        imagePanel.add(new JLabel("제출한 그림"), BorderLayout.NORTH);
        imagePanel.add(drawingLabel, BorderLayout.CENTER);

        topPanel.add(textPanel);
        topPanel.add(imagePanel);

        // 실제 정답과 제출한 답을 세로로 배치
        JPanel answerPanel = new JPanel(new GridLayout(2, 1, 5, 5));

        answerLabel.setForeground(Color.RED);
        answerLabel.setFont(
                answerLabel.getFont().deriveFont(Font.BOLD)
        );

        answerPanel.add(answerLabel);
        answerPanel.add(submittedAnswerLabel);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.add(topPanel, BorderLayout.CENTER);
        contentPanel.add(answerPanel, BorderLayout.SOUTH);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1));
        buttonPanel.add(continueStatusLabel);
        buttonPanel.add(nextButton);

        add(resultLabel, BorderLayout.NORTH);
        add(contentPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // 그림 영역 크기가 변경되면 이미지 크기 재조절
        drawingLabel.addComponentListener(
                new java.awt.event.ComponentAdapter() {
                    @Override
                    public void componentResized(
                            java.awt.event.ComponentEvent e) {
                        updateDrawingImage();
                    }
                }
        );
    }

    // 서버에서 전달받은 라운드 결과 표시
    public void setResult(
            String answer,
            String description,
            byte[] drawingData,
            String submittedAnswer,
            boolean correct) {

        resultLabel.setText(correct ? "정답입니다!" : "오답입니다.");
        answerLabel.setText("실제 정답: " + answer);
        descriptionArea.setText(description);
        submittedAnswerLabel.setText("제출한 답: " + submittedAnswer);

        originalDrawing = null;
        
        if (drawingData == null || drawingData.length == 0) {
            drawingLabel.setIcon(null);
            drawingLabel.setText("제출된 그림이 없습니다.");
        } else{
        	try {
        		BufferedImage image = ImageIO.read(new ByteArrayInputStream(drawingData));

        		if (image != null) {
        			originalDrawing = image;
                    drawingLabel.setText("");
                    updateDrawingImage();
        		} else {
        			drawingLabel.setIcon(null);
        			drawingLabel.setText("그림이 없습니다.");
        		}

        	} catch (IOException e) {
        		drawingLabel.setIcon(null);
        		drawingLabel.setText("그림을 불러오지 못했습니다.");
        	}
        }
        //continueStatusLabel.setText("다음 라운드 진행 확인: 0/3");
        //nextButton.setText("다음 라운드");
        nextButton.setEnabled(true);
    }
    
    private void updateDrawingImage() {
        if (originalDrawing == null) {
            return;
        }

        int labelWidth = drawingLabel.getWidth();
        int labelHeight = drawingLabel.getHeight();

        if (labelWidth <= 0 || labelHeight <= 0) {
            return;
        }

        double scale = Math.min(
                (double) labelWidth / originalDrawing.getWidth(),
                (double) labelHeight / originalDrawing.getHeight()
        );

        int width = (int) (originalDrawing.getWidth() * scale);
        int height = (int) (originalDrawing.getHeight() * scale);

        Image scaledImage = originalDrawing.getScaledInstance(
                width,
                height,
                Image.SCALE_SMOOTH
        );

        drawingLabel.setIcon(new ImageIcon(scaledImage));
    }
    
    public JButton getNextButton() {
        return nextButton;
    }
    
    public void setContinueStatus(int agreedCount, int playerCount) {
        continueStatusLabel.setText(
            "다음 라운드 진행 확인: "
                    + agreedCount + "/" + playerCount
        );
    }
}

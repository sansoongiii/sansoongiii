package client.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class GameFrame extends JFrame {

	private final CardLayout cardLayout;
    private final JPanel mainPanel;

    private final JLabel roundLabel;
    private final JLabel timeLabel;
    
    private final TextWriterPanel textWriterPanel;
    private final DrawingPanel drawingPanel;
    private final AnswererPanel answererPanel;
    private final ResultPanel resultPanel;
    private final JLabel phaseLabel;
    private Timer countdownTimer;

    public GameFrame() {
        setTitle("삼숭이 게임🙈🙉🐵");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(5, 5));

        // 상단 정보 영역
        JPanel infoPanel = new JPanel(new BorderLayout());

        roundLabel = new JLabel("라운드 1", SwingConstants.LEFT);
        phaseLabel = new JLabel("대기 중", SwingConstants.CENTER);
        timeLabel = new JLabel("남은 시간: --초", SwingConstants.RIGHT);

        infoPanel.add(roundLabel, BorderLayout.WEST);
        infoPanel.add(phaseLabel, BorderLayout.CENTER);
        infoPanel.add(timeLabel, BorderLayout.EAST);

        // 화면 전환 영역
        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        textWriterPanel = new TextWriterPanel();
        drawingPanel = new DrawingPanel();
        answererPanel = new AnswererPanel();
        resultPanel = new ResultPanel();

        mainPanel.add(textWriterPanel, "TEXT_WRITER");
        mainPanel.add(drawingPanel, "DRAWER");
        mainPanel.add(answererPanel, "ANSWERER");
        mainPanel.add(resultPanel, "RESULT");

        add(infoPanel, BorderLayout.NORTH);
        add(mainPanel, BorderLayout.CENTER);

        setSize(700, 550);
        setLocationRelativeTo(null);
    }
    
    // 화면 전환
    public void showScreen(String screenName) {
        cardLayout.show(mainPanel, screenName);
    }
    
    public void setRound(int round) {
        roundLabel.setText("라운드 " + round);
    }
    
    public void setPhase(String phase) {
        phaseLabel.setText(phase);
    }
    
    public void setRemainingTime(int seconds) {
        timeLabel.setText("남은 시간: " + seconds + "초");
    }
    
    public void resetRemainingTime() {
        timeLabel.setText("남은 시간: --초");
    }

    public TextWriterPanel getTextWriterPanel() {
        return textWriterPanel;
    }

    public DrawingPanel getDrawingPanel() {
        return drawingPanel;
    }

    public AnswererPanel getAnswererPanel() {
        return answererPanel;
    }

    public ResultPanel getResultPanel() {
        return resultPanel;
    }
    
    public void showTextWriter(int round, String word) {
        setRound(round);
        textWriterPanel.clearInput();
        textWriterPanel.setWord(word);
        showScreen("TEXT_WRITER");
    }

    public void showDrawing(int round, String description) {
        setRound(round);
        drawingPanel.clearCanvas();
        drawingPanel.setDescription(description);
        drawingPanel.setCanDraw(true);
        showScreen("DRAWER");
    }

    public void showAnswerer(int round, byte[] drawingData) {
        setRound(round);
        answererPanel.clearInput();
        answererPanel.setDrawing(drawingData);
        showScreen("ANSWERER");
    }

    public void showResult(
            int round,
            String answer,
            String description,
            byte[] drawingData,
            String submittedAnswer,
            boolean correct) {

    	stopCountdown();
        resetRemainingTime();
    	
        setRound(round);

        resultPanel.setResult(
            answer,
            description,
            drawingData,
            submittedAnswer,
            correct
        );

        showScreen("RESULT");
    }
    
    public void startCountdown(long deadlineEpochMillis, Runnable onTimeout) {
        stopCountdown();

        countdownTimer = new javax.swing.Timer(1000, e -> {

            long remainingMillis =
                deadlineEpochMillis - System.currentTimeMillis();

            int remainingSeconds =
                (int) Math.ceil(remainingMillis / 1000.0);

            if (remainingSeconds <= 0) {
                setRemainingTime(0);
                countdownTimer.stop();
                countdownTimer = null;

                onTimeout.run();
                return;
            }

            setRemainingTime(remainingSeconds);
        });

        countdownTimer.setInitialDelay(0);
        countdownTimer.start();
    }

    public void stopCountdown() {
        if (countdownTimer != null) {
            countdownTimer.stop();
            countdownTimer = null;
        }
    }
}
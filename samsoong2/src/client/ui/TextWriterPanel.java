package client.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class TextWriterPanel extends JPanel {

    private final JLabel wordLabel;
    private final JTextArea descriptionArea;
    private final JButton submitButton;
    private final Timer saveTimer;
    private Consumer<String> progressListener;

    public TextWriterPanel() {
        setLayout(new BorderLayout(10, 10));

        wordLabel = new JLabel("", SwingConstants.CENTER);
        descriptionArea = new JTextArea(5, 30);
        submitButton = new JButton("설명 제출");

        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        
        saveTimer = new Timer(500, e -> saveProgress());
        saveTimer.setRepeats(false);
        
        descriptionArea.getDocument().addDocumentListener(
        	    new DocumentListener() {
        	        @Override
        	        public void insertUpdate(DocumentEvent e) {
        	            saveProgress();
        	        }

        	        @Override
        	        public void removeUpdate(DocumentEvent e) {
        	            saveProgress();
        	        }

        	        @Override
        	        public void changedUpdate(DocumentEvent e) {
        	            saveProgress();
        	        }
        	    }
        	);

        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.add(new JLabel("제시어를 보고 설명을 입력하세요."), BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(submitButton);

        add(wordLabel, BorderLayout.NORTH);
        add(inputPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void scheduleSave() {
        saveTimer.restart();
    }

    private void saveProgress() {
        if (progressListener != null && descriptionArea.isEditable()) {
            progressListener.accept(descriptionArea.getText());
        }
    }
    
    public void setProgressListener(Consumer<String> listener) {
        this.progressListener = listener;
    }

    // 제시어 표시
    public void setWord(String word) {
        wordLabel.setText("제시어: " + word);
    }

    // 입력한 설명 가져오기
    public String getDescription() {
        return descriptionArea.getText().trim();
    }

    // 입력창 초기화
    public void clearInput() {
        descriptionArea.setText("");
        setInputEnabled(true);
    }

    // 제출 버튼 가져오기
    public JButton getSubmitButton() {
        return submitButton;
    }
    
    public void setInputEnabled(boolean enabled) {
    	descriptionArea.setEditable(enabled);
        submitButton.setEnabled(enabled);
    }
}
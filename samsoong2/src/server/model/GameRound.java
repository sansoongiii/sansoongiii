package server.model;

import java.util.Map;
import common.GamePhase;
import common.PlayerRole;

public class GameRound {

    private final int roundNumber;
    private final String category;
    private final String answer;
    private final Map<String, String> assignments;

    private String phase = GamePhase.TEXT_WRITING.name();

    private String textDescription;
    private byte[] drawingData;
    private String submittedAnswer;

    private boolean textSubmitted = false;
    private boolean drawingSubmitted = false;
    private boolean answerSubmitted = false;
    
    private final java.util.Set<String> readyPlayers = new java.util.HashSet<>();
    
    public GameRound(int roundNumber,String category,String answer,Map<String, String> assignments) {
        if (roundNumber < 1) {
            throw new IllegalArgumentException("라운드 번호는 1 이상이어야 합니다.");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("제시어는 필수입니다.");
        }

        if (assignments == null || assignments.size() != 3) {
            throw new IllegalArgumentException("역할 배정 정보가 올바르지 않습니다.");
        }
        this.roundNumber = roundNumber;
        this.category = category;
        this.answer = answer;
        this.assignments = Map.copyOf(assignments);
    }
    public String getAnswer() {
        return answer;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public String getCategory() {
        return category;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public boolean isAssignedTo(String playerId, String role) {
        return playerId != null&& role != null&& role.equals(assignments.get(playerId));
    }

    public String getAnswerFor(String playerId) {
        if (!isAssignedTo(playerId, PlayerRole.TEXT_WRITER.name())) {
            throw new SecurityException("제시어를 확인할 권한이 없습니다.");
        }
        return answer;
    }

    public boolean matchesAnswer(String guess) {
        return guess != null&& answer.trim().equalsIgnoreCase(guess.trim());
    }

    public void submitText(String description) {
    	if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("설명은 비어 있을 수 없습니다.");
        }
        if (textSubmitted) {
            throw new IllegalStateException("이미 설명을 제출했습니다.");
        }
        this.textDescription = description;
        this.textSubmitted = true;
    }
    
    public boolean isTextSubmitted() {
        return textSubmitted;
    }

    public String getTextDescription() {
        return textDescription;
    }

    public void submitDrawing(byte[] drawingData) {
    	if (drawingData == null) {
            throw new IllegalArgumentException("그림 데이터는 null일 수 없습니다.");
        }
        if (drawingSubmitted) {
            throw new IllegalStateException("이미 그림을 제출했습니다.");
        }
        this.drawingData = drawingData.clone();
        this.drawingSubmitted = true;
    }
    
    public boolean isDrawingSubmitted() {
        return drawingSubmitted;
    }

    public byte[] getDrawingData() {
        return drawingData == null? null: drawingData.clone();
    }

    public void submitAnswer(String submittedAnswer) {
    	if (submittedAnswer == null) {
            throw new IllegalArgumentException("정답 데이터는 null일 수 없습니다.");
        }

        if (answerSubmitted) {
            throw new IllegalStateException("이미 정답을 제출했습니다.");
        }
        this.submittedAnswer = submittedAnswer;
        this.answerSubmitted = true;
    }

    public boolean isAnswerSubmitted() {
        return answerSubmitted;
    }

    public String getSubmittedAnswer() {
        return submittedAnswer;
    }

    public Map<String, String> getAssignments() {
        return assignments;
    }
    // 설명 작성 중인 내용 저장
    public void updateTextDescription(String textDescription) {
    	if (textSubmitted) {
            return;
        }
        this.textDescription = textDescription;
    }
    // 그림 작성 중인 내용 저장
    public void updateDrawingData(byte[] drawingData) {
    	if (drawingSubmitted) {
            return;
        }
        this.drawingData = drawingData == null? null: drawingData.clone();
    }
    // 정답 작성 중인 내용 저장
    public void updateSubmittedAnswer(String submittedAnswer) {
    	if (answerSubmitted) {
            return;
        }
        this.submittedAnswer = submittedAnswer;
    }
    
    // 라운드 결과 확인 완료 등록
    public synchronized void markPlayerReady(String playerId) {
        if (playerId == null || !assignments.containsKey(playerId)) {
            throw new IllegalArgumentException("유효하지 않은 플레이어입니다.");
        }
        readyPlayers.add(playerId);
    }

    // 모든 플레이어가 확인했는지 검사
    public synchronized boolean areAllPlayersReady() {
        return readyPlayers.size() == assignments.size();
    }
    
    public synchronized int getReadyCount() {
        return readyPlayers.size();
    }
}

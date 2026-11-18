package common;

import java.util.ArrayList;
import java.util.List;

public class Message {

	private MessageType type;
	
	private String playerId;
    private String roomCode;
    private int playerCount;

    private String category;
    private List<String> categories = new ArrayList<>();
    
    private int roundNumber;
    private String role;
    private String phase;
    private long deadlineEpochMillis;

    private String answer;
    private String textDescription;
    private byte[] drawingData;
    private String submittedAnswer;
    private boolean correct;
    
    private boolean agree;
    private int agreedCount;

    private String errorCode;
    private String notice;
    
    public Message() {
       
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        if (type == null) {
            throw new IllegalArgumentException(
                    "메시지 종류가 없습니다."
            );
        }
        this.type = type;
    }
    
    public String getPlayerId() {
        return playerId;
    }

    public void setPlayerId(String playerId) {
        this.playerId = playerId;
    }

    public String getRoomCode() {
        return roomCode;
    }

    public void setRoomCode(String roomCode) {
        this.roomCode = roomCode;
    }

    public int getPlayerCount() {
        return playerCount;
    }

    public void setPlayerCount(int playerCount) {
        if (playerCount < 0) {
            throw new IllegalArgumentException(
                    "참가자 수는 0 이상이어야 합니다."
            );
        }
        this.playerCount = playerCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<String> getCategories() {
        return new ArrayList<>(categories);
    }

    public void setCategories(List<String> categories) {
        this.categories = categories == null
                ? new ArrayList<>()
                : new ArrayList<>(categories);
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public void setRoundNumber(int roundNumber) {
        if (roundNumber < 0) {
            throw new IllegalArgumentException(
                    "라운드 번호는 0 이상이어야 합니다."
            );
        }
        this.roundNumber = roundNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }

    public long getDeadlineEpochMillis() {
        return deadlineEpochMillis;
    }

    public void setDeadlineEpochMillis(long deadlineEpochMillis) {
        if (deadlineEpochMillis < 0) {
            throw new IllegalArgumentException(
                    "종료 시각은 0 이상이어야 합니다."
            );
        }
        this.deadlineEpochMillis = deadlineEpochMillis;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getTextDescription() {
        return textDescription;
    }

    public void setTextDescription(String textDescription) {
        this.textDescription = textDescription;
    }

    public byte[] getDrawingData() {
        return drawingData == null ? null : drawingData.clone();
    }

    public void setDrawingData(byte[] drawingData) {
        this.drawingData =
                drawingData == null ? null : drawingData.clone();
    }

    public String getSubmittedAnswer() {
        return submittedAnswer;
    }

    public void setSubmittedAnswer(String submittedAnswer) {
        this.submittedAnswer = submittedAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public void setCorrect(boolean correct) {
        this.correct = correct;
    }

    public boolean isAgree() {
        return agree;
    }

    public void setAgree(boolean agree) {
        this.agree = agree;
    }

    public int getAgreedCount() {
        return agreedCount;
    }

    public void setAgreedCount(int agreedCount) {
        if (agreedCount < 0) {
            throw new IllegalArgumentException(
                    "동의 인원은 0 이상이어야 합니다."
            );
        }
        this.agreedCount = agreedCount;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getNotice() {
        return notice;
    }

    public void setNotice(String notice) {
        this.notice = notice;
    }
}
  	
	

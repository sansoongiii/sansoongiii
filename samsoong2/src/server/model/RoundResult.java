package server.model;

public class RoundResult {

    private final int roundNumber;
    private final String answer;
    private final String textDescription;
    private final byte[] drawingData;
    private final String submittedAnswer;
    private final boolean correct;

    public RoundResult(int roundNumber,String answer,String textDescription,byte[] drawingData,String submittedAnswer,boolean correct) {

        this.roundNumber = roundNumber;
        this.answer = answer;
        this.textDescription = textDescription;
        this.drawingData = drawingData == null? null: drawingData.clone();
        this.submittedAnswer = submittedAnswer;
        this.correct = correct;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    public String getAnswer() {
        return answer;
    }

    public String getTextDescription() {
        return textDescription;
    }

    public byte[] getDrawingData() {
        return drawingData == null? null: drawingData.clone();
    }

    public String getSubmittedAnswer() {
        return submittedAnswer;
    }

    public boolean isCorrect() {
        return correct;
    }
}
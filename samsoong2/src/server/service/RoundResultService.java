package server.service;

import java.util.Objects;

import server.model.GameRoom;
import server.model.GameRound;
import server.model.RoundResult;

public class RoundResultService {
    private final CheckAnswerService checkAnswerService;

    public RoundResultService(CheckAnswerService checkAnswerService) {
        this.checkAnswerService = Objects.requireNonNull(
                checkAnswerService,
                "CheckAnswerService는 필수입니다."
        );
    }

    // 현재 라운드의 결과 생성
    public RoundResult createRoundResult(GameRoom room) {
        GameRound round = room.getCurrentRound();

        String submittedAnswer = round.getSubmittedAnswer();

        boolean correct = submittedAnswer != null&& !submittedAnswer.isBlank()&& checkAnswerService.checkAnswer(round, submittedAnswer);
        String answer = round.getAnswer();
        String textDescription = round.getTextDescription();
        byte[] drawingData = round.getDrawingData();

        return new RoundResult(round.getRoundNumber(),answer,textDescription,drawingData,submittedAnswer,correct
        );
    }
}
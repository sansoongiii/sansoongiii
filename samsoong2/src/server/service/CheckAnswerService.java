package server.service;

import server.model.GameRound;

public class CheckAnswerService {
    public boolean checkAnswer(GameRound round, String submittedAnswer) {
        return round.matchesAnswer(submittedAnswer);
    }
}
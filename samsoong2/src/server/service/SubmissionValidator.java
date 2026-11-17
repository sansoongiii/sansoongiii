package server.service;

import common.PlayerRole;
import server.model.GameRound;

public class SubmissionValidator {

    // 플레이어가 해당 역할을 가지고 있는지 검사
    public void validateRole(GameRound round,String playerId,String requiredRole) {
        if (playerId == null|| !requiredRole.equals(round.getAssignments().get(playerId))) {
            throw new SecurityException("해당 작업을 수행할 권한이 없습니다.");
        }
    }

    // 현재 게임 단계가 올바른지 검사
    public void validatePhase(GameRound round,String requiredPhase) {
        if (!requiredPhase.equals(round.getPhase())) {
            throw new IllegalStateException("현재 게임 단계에서는 제출할 수 없습니다.");
        }
    }

    // 설명 입력값 검사
    public void validateDescription(GameRound round,String textDescription) {
        if (textDescription == null || textDescription.isBlank()) {
            throw new IllegalArgumentException("설명을 입력해 주세요.");
        }

        String answer = round.getAnswerFor(getPlayerIdByRole(round, PlayerRole.TEXT_WRITER.name()));

        if (textDescription.contains(answer)) {
            throw new IllegalArgumentException("제시어를 그대로 사용할 수 없습니다.");
        }
    }

    // 그림 데이터 검사
    public void validateDrawing(byte[] drawingData) {
    	if (drawingData == null || drawingData.length == 0) {
            throw new IllegalArgumentException("그림 데이터가 없습니다.");
        }
    }

    // 정답 입력값 검사
    public void validateAnswer(String submittedAnswer) {
        if (submittedAnswer == null || submittedAnswer.isBlank()) {
            throw new IllegalArgumentException("정답을 입력해 주세요.");
        }
    }

    private String getPlayerIdByRole(GameRound round,String role) {
        return round.getAssignments().entrySet().stream().filter(entry -> role.equals(entry.getValue())).map(java.util.Map.Entry::getKey).findFirst()
            .orElseThrow(() -> new IllegalStateException("해당 역할의 플레이어가 없습니다."));
    }
}
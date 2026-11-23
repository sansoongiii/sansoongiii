package client.controller;

import java.util.*;
import common.Message; // 통합 시에 패키지 제대로 불러오게 해줘야 함 (독립으로 시행 시 오류로 뜸)
import common.PlayerRole;

public class GameController {

    public void onConnected(String playerId) {
        System.out.println("[접속 완료] 내 ID: " + playerId);
    }

    public void onCategoriesReceived(List<String> categories) {
        System.out.println("[카테고리 목록] " + categories);
    }

    public void onRoomUpdated(String roomCode, int playerCount, String category) {
        System.out.printf("[방 정보] 방코드: %s | 인원: %d/3 | 카테고리: %s%n", roomCode, playerCount, category);
    }

    // 첫 번째 순서에게만 제시어 출력
    public void onPhaseStarted(Message message) {
        System.out.printf("[라운드 %d 시작] 단계: %s | 내 역할: %s%n",
                message.getRoundNumber(), message.getPhase(), message.getRole());

        if (PlayerRole.TEXT_WRITER.name().equals(message.getRole())) {
            System.out.println("제시어: " + message.getAnswer());
        } else {
            System.out.println("다른 플레이어가 설명을 작성 중입니다..."); // 첫 번째 순서 외의 화면
        }
    }

    // 아래는 1차 통합 이후 사용할 메서드들 (시그니처만 맞춰둠)
    public void onRoundResult(Message message) { }
    public void onGameFinished(String notice) { System.out.println(notice); }
    public void onContinueStatus(int agreedCount, int playerCount, String notice) { }
    public void onRoomLeft(String notice) { System.out.println(notice); }
    public void onGameAborted(String notice) { System.out.println(notice); }
    public void onError(String errorCode, String notice) {
        System.out.println("[오류 " + errorCode + "] " + notice);
    }
}
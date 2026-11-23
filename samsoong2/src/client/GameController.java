package client;

import java.util.*;

import common.GamePhase;
import common.Message;
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
        String phase = message.getPhase();
        String role = message.getRole();

        System.out.printf("[라운드 %d 시작] 단계: %s | 내 역할: %s%n",
                message.getRoundNumber(), phase, role);

        if (GamePhase.TEXT_WRITING.name().equals(phase)) {
            if (PlayerRole.TEXT_WRITER.name().equals(role)) { // WRITER 차례
                System.out.println("제시어: " + message.getAnswer()); // WRITER 콘솔
                System.out.println("제시어에 대한 설명을 작성하세요!");
            } else {
                System.out.println("설명을 작성 중입니다..."); // 그 외 플레이어 콘솔
            }
        } else if (GamePhase.DRAWING.name().equals(phase)) { // DRAWER 차례
            if (PlayerRole.DRAWER.name().equals(role)) {
                System.out.println(message.getTextDescription());
                System.out.println("설명을 읽고 그림을 그리세요!");
            } else {
                System.out.println("그림을 그리는 중입니다...");
            }
        } else if (GamePhase.ANSWERING.name().equals(phase)) { // ANSWERER 차례
            if (PlayerRole.ANSWERER.name().equals(role)) {
            	// 그림 전달 (ViewController에서 담당)
                System.out.println("그림을 보고 정답을 입력하세요!");
            } else {
                System.out.println("정답을 고민하는 중입니다...");
            }
        }
    }

    public void onRoundResult(Message message) {
        System.out.printf("[%d 라운드 결과] %s %n", message.getRoundNumber(), message.isCorrect() ? "정답!" : "오답!");
        System.out.println(" - 제시어: " + message.getAnswer());
        System.out.println(" - 설명: " + message.getTextDescription());
        // 그림 전달
        System.out.println(" - 제출한 답안: " + message.getSubmittedAnswer());
    }

    public void onContinueStatus(int agreedCount, int playerCount, String notice) {
        System.out.printf("[게임 계속하기] %d/%d명 (%s)%n", agreedCount, playerCount, notice);
    }

    public void onGameFinished(String notice) {
        System.out.println("[게임 종료] " + notice);
    }

    public void onRoomLeft(String notice) {
        System.out.println("[방 퇴장] " + notice);
    }

    public void onGameAborted(String notice) {
        System.out.println("[게임 중단] " + notice);
    }

    public void onError(String errorCode, String notice) {
        System.out.println("[오류 " + errorCode + "] " + notice);
    }

    public void onDisconnected() {
        System.out.println("[서버 연결 종료] 서버와의 연결이 끊어졌습니다.");
    }
}
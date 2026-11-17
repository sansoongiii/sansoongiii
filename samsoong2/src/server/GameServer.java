package server;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.net.ServerSocket;

public class GameServer {

    public static final int PORT = 50023;
    
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    private final Map<String, ClientHandler> clients =
            new ConcurrentHashMap<>();

    public void start() {
        // ServerSocket 생성
        // accept()로 접속 수락
        // ClientHandler 생성 및 연결 목록 등록
        // 별도 스레드에서 실행
    }

    public void sendTo(String playerId, String message) {
        // 특정 참가자에게 전송
    }

    public void sendToPlayers(
            Iterable<String> playerIds, String message) {
        // 지정된 참가자들에게 전송
        // 예: 같은 방의 참가자 전체
    }

    public void removeClient(String playerId) {
        // 연결 목록에서 제거
    }

    public void stop() {
        // 접속 대기 종료
        // 모든 클라이언트 연결 종료
    }
}
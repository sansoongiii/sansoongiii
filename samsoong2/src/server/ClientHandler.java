package server;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private final String playerId;
    private final Socket socket;
    private final GameServer server;

    private BufferedReader reader;
    private PrintWriter writer;

    private volatile boolean connected = false;

    public ClientHandler(
            String playerId, Socket socket, GameServer server) {

        this.playerId = playerId;
        this.socket = socket;
        this.server = server;
    }

    @Override
    public void run() {
        // 입출력 스트림 생성
        // 클라이언트 메시지를 반복해서 수신
        // 요청을 게임 로직에 전달
        // 연결 종료 시 close() 호출
    }

    public synchronized void send(String message) {
        // 해당 클라이언트에게 전송
    }

    public String getPlayerId() {
        return playerId;
    }

    public void close() {
        // 연결 상태 변경
        // Socket 종료
        // server.removeClient(playerId) 호출
        // 게임 로직에 연결 종료 통보
        // 중복 정리 방지
    }
}
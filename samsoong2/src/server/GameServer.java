package server;

import common.GameMessageSender;
import common.Message;
import common.Protocol;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import java.util.ArrayList;
import java.util.List;

public class GameServer implements GameMessageSender{

    public static final int PORT = 50023;
    
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    private final Map<String, ClientHandler> clients =
            new ConcurrentHashMap<>();

    public void start() throws IOException {
        // ServerSocket 생성
    	serverSocket = new ServerSocket(Protocol.PORT);
    	running = true;
    	System.out.println("서버 시작: 포트 " + Protocol.PORT);

    	while (running) {
	        // accept()로 접속 수락
			Socket socket = serverSocket.accept();
			String playerId = UUID.randomUUID().toString();
			
	        // ClientHandler 생성 및 연결 목록 등록
			ClientHandler handler = new ClientHandler(playerId, socket, this);
			clients.put(playerId, handler);
			
			// 별도 스레드에서 실행
			Thread thread = new Thread(handler);
			thread.start();
			
			System.out.println("클라이언트 접속: " + playerId);
		}     
    }


    public void removeClient(String playerId) {
        ClientHandler removed = clients.remove(playerId);

        if (removed != null) {
            System.out.println(
                    "연결 목록에서 제거: " + playerId
            );
        }
    }

    public void stop() {
        // 접속 대기 종료
        // 모든 클라이언트 연결 종료
    }

    @Override
    public void sendTo(String playerId, Message message) {
        ClientHandler handler = clients.get(playerId);

        if (handler == null) {
            System.out.println(
                    "전송 대상이 없습니다: " + playerId
            );
            return;
        }

        handler.send(message);
    }

    @Override
    public void sendToPlayers(
            Iterable<String> playerIds,
            Message message) {

        for (String playerId : playerIds) {
            sendTo(playerId, message);
        }
    }
}
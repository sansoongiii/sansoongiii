package server;

import common.Message;
import common.MessageType;
import common.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ProtocolException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

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
    	try {
    		reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
    		writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
    		
    		connected = true;
    		writer.println(Protocol.build(new Message(MessageType.CONNECTED, playerId)));
    		
    		String line;
    		while ((line = reader.readLine()) != null) {
				try {
					Message message = Protocol.parse(line);
					System.out.println("보낸 참가자: " + playerId);
					System.out.println("메시지 종류: " + message.getType());
					System.out.println("데이터: " + message.getData());
					
					//이후 여기에서 A의 요청 처리 코드 호출 
				} catch (IllegalArgumentException e) {
					System.out.println("잘못된 메시지: " + e.getMessage());
				}
			}
    	} catch (IOException e) {
    		System.out.println("수신 오류: " + e.getMessage());
    	} finally {
    		connected = false;
    		
    		try {
                socket.close();
            } catch (IOException e) {
                System.out.println(
                        "연결 종료 오류: " + e.getMessage()
                );
            }
    	}
    }

    public synchronized void send(Message message) {
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
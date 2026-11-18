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
    		Message connectedMessage = new Message();
    		connectedMessage.setType(MessageType.CONNECTED);
    		connectedMessage.setPlayerId(playerId);

    		server.sendTo(playerId, connectedMessage); 
    		
    		String line;
    		while ((line = reader.readLine()) != null) {
				try {
					Message message = Protocol.parse(line);

					System.out.println("보낸 참가자: " + playerId);
					System.out.println("메시지 종류: " + message.getType());

					if (message.getType() == MessageType.JOIN_ROOM) {
					    System.out.println(
					            "요청한 방 코드: " + message.getRoomCode()
					    );
					}

					// 임시 통신 응답 — 실제 입장 성공을 의미하지 않음
					Message response = new Message();
					response.setType(MessageType.TEST_MESSAGE);
					response.setRoundNumber(message.getRoundNumber());
					response.setNotice(
					        "요청 수신 확인: " + message.getType().name()
					);

					server.sendTo(playerId, response);
					
				} catch (IllegalArgumentException e) {
					System.out.println("잘못된 메시지: " + e.getMessage());
				}
			}
    	} catch (IOException e) {
    		System.out.println("수신 오류: " + e.getMessage());
    	} finally {
    	    close();
    	}
    }

    public synchronized void send(Message message) {
        if (!connected || writer == null) {
            System.out.println(
                    "전송 불가: 연결이 준비되지 않았습니다. "
                            + playerId
            );
            return;
        }

        writer.println(Protocol.build(message));

        if (writer.checkError()) {
            System.out.println(
                    "메시지 전송 실패: " + playerId
            );
        }
        
    }

    public String getPlayerId() {
        return playerId;
    }

    public synchronized void close() {
        connected = false;

        try {
            if (!socket.isClosed()) {
                socket.close();
            }
        } catch (IOException e) {
            System.out.println(
                    "연결 종료 오류: " + e.getMessage()
            );
        } finally {
            server.removeClient(playerId);
        }
    }
}
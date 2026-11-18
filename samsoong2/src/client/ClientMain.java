package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import common.Message;
import common.MessageType;

public class ClientMain {

    public static void main(String[] args) {

    	GameClient client = new GameClient();
    	
    	BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    	
    	try {
            client.connect("127.0.0.1");
            client.startReceiving();
            
            System.out.println("1: 방 생성 / 2: 방 입장");
            String choice = keyboard.readLine();

            Message request = new Message();
            
            if ("1".equals(choice)) {
				request.setType(MessageType.CREATE_ROOM);
			} else if("2".equals(choice)) {
				System.out.println("방 코드 입력: ");
				String roomCode = keyboard.readLine();
				
				if(roomCode == null || roomCode.isBlank()) {
					System.out.println("방 코드를 입력해야 합니다.");
					client.close();
					return;
				}
				
				request.setType(MessageType.JOIN_ROOM);
				request.setRoomCode(roomCode.trim());
			} else {
				System.out.println("1 또는 2를 선택해야 합니다.");
				client.close();
				return;
			}
        
            client.send(request);

        } catch (IOException e) {
            System.out.println(
                    "연결 또는 입출력 오류: " + e.getMessage()
            );

            client.close();
        }
    }
}
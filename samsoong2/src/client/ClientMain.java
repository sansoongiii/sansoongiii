package client;

import java.io.IOException;

import common.Message;
import common.MessageType;

public class ClientMain {

    public static void main(String[] args) {

    	GameClient client = new GameClient();
    	
    	try {
            client.connect("127.0.0.1");
            client.startReceiving();

            Message request = new Message();
            request.setType(MessageType.JOIN_ROOM);
            request.setRoomCode("ABC123");

            client.send(request);

        } catch (IOException e) {
            System.out.println(
                    "연결 또는 전송 실패: " + e.getMessage()
            );

            client.close();
        }
    }
}
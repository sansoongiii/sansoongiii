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
			client.send(new Message(MessageType.JOIN_ROOM, "ABC123"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("서버 연결 실패: " + e.getMessage());
			client.close();
		}
    }
}
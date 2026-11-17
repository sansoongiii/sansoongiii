package client;

import java.io.BufferedReader;
import java.io.IOException;

import common.Message;
import common.Protocol;

public class ClientReceiver implements Runnable {

    private final BufferedReader reader;
    private final GameClient client;

    public ClientReceiver(
            BufferedReader reader, GameClient client) {

        this.reader = reader;
        this.client = client;
    }

    @Override
    public void run() {
       try {
    	   String line; 
    	   while((line = reader.readLine()) != null) {
    		   try {
    			   Message message = Protocol.parse(line);
    			   System.out.println("[서버 메시지] " + message.getType());
    			   System.out.println("[내용] " + message.getData());
    		   } catch (IllegalArgumentException e) {
    			   System.out.println("메시지 형식 오류: " + e.getMessage());
    		   }
    	   }
    	   if (client.isConnected()) {
			System.out.println("서버 연결이 종료되었습니다.");
		}
       } catch (IOException e) {
    	   if (client.isConnected()) {
			System.out.println("수신 오류: " + e.getMessage());
		}
       } finally {
    	   client.close();
       }
    }
}
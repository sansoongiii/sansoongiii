package server;

import java.io.IOException;

public class ServerMain {

    public static void main(String[] args) {
		// TODO Auto-generated method stub

    	GameServer server = new GameServer();
    	
    	try {
			server.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("서버 실행 오류: " + e.getMessage());
		}
    	
    }
}
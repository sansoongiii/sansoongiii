package client;

import java.io.IOException;

public class ClientMain {

    public static void main(String[] args) {
        // GameClient 생성
    	GameClient client = new GameClient();
        // 서버 IP 입력 및 connect() 호출
    	try {
			client.connect("127.0.0.1");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("서버 연결 실패: " + e.getMessage());
		}
        // startReceiving() 호출
        // 테스트용 키보드 입력을 send()로 전달
        // 종료 시 close() 호출
    }
}
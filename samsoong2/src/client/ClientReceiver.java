package client;

import java.io.BufferedReader;

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
        // 서버 메시지를 반복해서 수신
        // 수신 내용을 화면 담당 코드에 전달
        // 연결 종료 시 client.close() 호출
    }
}
package client;

import common.Message;
import common.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

public class GameClient {

    private static final int SERVER_PORT = 50023;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread receiveThread;

    private volatile boolean connected = false;

    public void connect(String serverIp) throws IOException {
    	socket = new Socket();
    	try {
            socket.connect(new InetSocketAddress(serverIp,Protocol.PORT), 5000);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            connected = true;
            System.out.println("서버 연결 완료!");

        } catch (IOException e) {
            socket.close();
            throw e;
        }    	
    	
    }

    public void startReceiving() {
        // ClientReceiver 생성
        // 별도 스레드에서 실행
        // 중복 실행 방지
    }

    public synchronized void send(Message message) {
        // 서버에 메시지 전송
    }

    public boolean isConnected() {
        return connected;
    }

    public void close() {
        // 연결 상태 변경
        // Socket 종료
    }
}
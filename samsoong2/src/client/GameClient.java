package client;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

public class GameClient {

    private static final int SERVER_PORT = 50023;

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread receiveThread;

    private volatile boolean connected = false;

    public void connect(String serverIp) {
        // 서버 연결
        // 입출력 스트림 생성
        // 연결 상태 변경
    }

    public void startReceiving() {
        // ClientReceiver 생성
        // 별도 스레드에서 실행
        // 중복 실행 방지
    }

    public synchronized void send(String message) {
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
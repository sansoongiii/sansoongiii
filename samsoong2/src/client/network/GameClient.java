package client.network;

import common.Message;
import common.MessageType;
import common.Protocol;

import client.controller.GameController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameClient {

	private Socket socket;
	private BufferedReader reader;
	private PrintWriter writer;
	private Thread receiveThread;

	private volatile boolean connected;

	private final GameController controller;
	private final AtomicBoolean closed = new AtomicBoolean(false);

	public GameClient() {
		this(new GameController());
	}

	public GameClient(GameController controller) {
		this.controller = Objects.requireNonNull(controller);
	}

	public void connect(String serverIp) throws IOException {

		if (closed.get()) {
			throw new IOException("종료한 클라이언트입니다. 새 객체로 연결하세요.");
		}

		if (socket != null) {
			throw new IOException("이미 연결을 시도한 클라이언트입니다.");
		}

		socket = new Socket();

		try {
			socket.connect(new InetSocketAddress(serverIp, Protocol.PORT), 5000);

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
		if (!connected) {
			throw new IllegalStateException("먼저 서버에 연결해야 합니다.");
		}
		if (receiveThread != null) {
			return;
		}
		ClientReceiver receiver = new ClientReceiver(reader, this, controller);
		receiveThread = new Thread(receiver, "client-receiver");
		receiveThread.start();
	}

	public synchronized void send(Message message) throws IOException {
		// 서버에 메시지 전송
		if (!connected) {
			throw new IOException("서버에 연결되어 있지 않습니다.");
		}
		writer.println(Protocol.build(message));
		if (writer.checkError()) {
			throw new IOException("메시지 전송에 실패했습니다.");
		}
	}

	public boolean isConnected() {
		return connected;
	}

	public synchronized void close() {
		if (!closed.compareAndSet(false, true)) {
			return;
		}

		connected = false;

		try {
			if (socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("연결 종료 오류: " + e.getMessage());
		}
	}

	public void disconnect() {
		try {
			if (connected) {
				Message request = new Message();
				request.setType(MessageType.DISCONNECT);
				send(request);
			}
		} catch (IOException e) {
			System.out.println("종료 요청 전송 실패: " + e.getMessage());
		} finally {
			close();
		}
	}


}
package client.network;

import java.io.BufferedReader;
import java.io.IOException;

import javax.swing.SwingUtilities;

import client.command.ClientCommand;
import client.command.ClientCommandFactory;
import client.controller.GameController;

import common.Message;
import common.Protocol;

public class ClientReceiver implements Runnable {

	private final BufferedReader reader;
	private final GameClient client;
	private final GameController controller;
	private final ClientCommandFactory commandFactory = new ClientCommandFactory();

	public ClientReceiver(BufferedReader reader, GameClient client, GameController controller) {
		this.reader = reader;
		this.client = client;
		this.controller = controller;
	}

	@Override
	public void run() {
		try {
			String line;

			while ((line = reader.readLine()) != null) {
				Message message;

				try {
					message = Protocol.parse(line);
				} catch (IllegalArgumentException e) {
					System.out.println("메시지 형식 오류: " + e.getMessage());
					continue;
				}
				receive(message);
			}
		} catch (IOException e) {
			if (client.isConnected()) {
				System.out.println("수신 오류: " + e.getMessage());
			}
		} finally {
			client.close();

			SwingUtilities.invokeLater(() -> controller.onDisconnected());
		}
	}

	private void receive(Message message) {
		ClientCommand command;
		try {
			command = commandFactory.create(message.getType());
		} catch (IllegalArgumentException e) {
			System.out.println("알림 처리 오류: " + e.getMessage());
			return;
		}

		// 화면·컨트롤러 처리는 Swing 이벤트 스레드에서 실행
		SwingUtilities.invokeLater(() -> {
			try {
				command.execute(controller, message);
			} catch (RuntimeException e) {
				System.out.println("컨트롤러 처리 오류: " + e.getMessage());
				e.printStackTrace();
			}
		});

	}
}

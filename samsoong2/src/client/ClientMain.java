package client;

import java.io.IOException;

import client.controller.ConnectionController;
import client.controller.GameController;
import client.controller.ViewController;
import client.network.GameClient;

public class ClientMain {

	public static void main(String[] args) {

		GameController gameController = new GameController();
		GameClient client = new GameClient(gameController);

		ConnectionController connectionController = new ConnectionController(client);
		gameController.setConnectionController(connectionController);
		
		ViewController viewController = new ViewController(client, connectionController, gameController);

		gameController.setViewController(viewController);

		try {
			client.connect("127.0.0.1");

			if (!viewController.initGui()) {
				client.close();
				return;
			}

			client.startReceiving();

		} catch (IOException e) {
			System.out.println("연결 또는 입출력 오류: " + e.getMessage());

			client.close();
			viewController.closeGui();
		}
	}

}
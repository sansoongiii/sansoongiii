package client;

import java.io.IOException;

import client.controller.ConnectionController;
import client.controller.ViewController;
import client.network.GameClient;
import common.Message;
import common.MessageType;

public class ClientMain {

    public static void main(String[] args) {

        GameClient client = new GameClient();

        try {
            client.connect("127.0.0.1");
            client.startReceiving();

            // GUI 기반 게임 진행 시작
            startGameGui(client);
        } catch (IOException e) {
            System.out.println(
                    "연결 또는 입출력 오류: " + e.getMessage()
            );

            client.close();
        }
    }

    private static void startGameGui(GameClient client) {
        ConnectionController conn = new ConnectionController(client);
        ViewController viewController = new ViewController(client, conn);

        if (!viewController.initGui()) {
            return;
        }

        Thread gameThread = new Thread(() -> {
            try {
                while (true) {
                    Message message = client.waitForPhaseStarted();
                    if (message == null) {
                        viewController.closeGui();
                        break;
                    }

                    viewController.handleMessage(message);

                    if (message.getType() == MessageType.GAME_FINISHED) {
                        break;
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        gameThread.start();
    }
}

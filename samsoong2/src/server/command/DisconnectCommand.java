package server.command;

import common.Message;
import server.network.ClientHandler;

public class DisconnectCommand implements ServerCommand {

    @Override
    public void execute(
            ClientHandler clientHandler,
            Message message) {

    	System.out.println("[종료 요청 처리] " + clientHandler.getPlayerId());
        clientHandler.close();
    }
}
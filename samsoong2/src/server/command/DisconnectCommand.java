package server.command;

import common.Message;
import server.network.ClientHandler;

public class DisconnectCommand implements ServerCommand {

    @Override
    public void execute(
            ClientHandler clientHandler,
            Message message) {

        clientHandler.close();
    }
}
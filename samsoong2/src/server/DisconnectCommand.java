package server;

import common.Message;

public class DisconnectCommand implements ServerCommand {

    @Override
    public void execute(
            ClientHandler clientHandler,
            Message message) {

        clientHandler.close();
    }
}
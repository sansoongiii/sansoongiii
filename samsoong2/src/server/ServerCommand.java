package server;

import common.Message;

public interface ServerCommand {

    void execute(ClientHandler clientHandler, Message message);
}

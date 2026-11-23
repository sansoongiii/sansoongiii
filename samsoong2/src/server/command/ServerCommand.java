package server.command;

import common.Message;
import server.network.ClientHandler;

public interface ServerCommand {

    void execute(ClientHandler clientHandler, Message message);
}

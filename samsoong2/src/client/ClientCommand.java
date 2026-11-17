package client;

import common.Message;

public interface ClientCommand {

    void execute(GameController controller, Message message);
}
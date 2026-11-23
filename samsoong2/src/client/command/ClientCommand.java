package client.command;

import client.controller.GameController;
import common.Message;

public interface ClientCommand {

    void execute(GameController controller, Message message);
}
package server.command;

import java.util.EnumMap;
import java.util.Map;

import common.MessageType;

public class ServerCommandFactory {

    private final Map<MessageType, ServerCommand> commands = new EnumMap<>(MessageType.class);

    public ServerCommandFactory() {
        register(MessageType.DISCONNECT, new DisconnectCommand() );
    }

    public void register(MessageType type, ServerCommand command) {
        commands.put(type, command);
    }

    public ServerCommand create(MessageType type) {
        ServerCommand command = commands.get(type);

        if (command == null) {
            throw new IllegalArgumentException(
                    "등록되지 않은 서버 명령: " + type
            );
        }

        return command;
    }
}
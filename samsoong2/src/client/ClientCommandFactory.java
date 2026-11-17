package client;

import java.util.EnumMap;
import java.util.Map;

import common.MessageType;

public class ClientCommandFactory {

    private final Map<MessageType, ClientCommand> commands = new EnumMap<>(MessageType.class);

    public ClientCommandFactory() {
        // 메시지 종류별 Command 등록
    }

    public void register(MessageType type, ClientCommand command) {
        commands.put(type, command);
    }

    public ClientCommand create(MessageType type) {
        ClientCommand command = commands.get(type);

        if (command == null) {
            throw new IllegalArgumentException(
                    "등록되지 않은 서버 알림: " + type
            );
        }

        return command;
    }
}
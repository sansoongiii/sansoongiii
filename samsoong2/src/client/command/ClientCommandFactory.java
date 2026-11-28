package client.command;

import java.util.EnumMap;
import java.util.Map;

import common.MessageType;

public class ClientCommandFactory {

    private final Map<MessageType, ClientCommand> commands = new EnumMap<>(MessageType.class);

    public ClientCommandFactory() {
    	register(MessageType.CONNECTED, (controller, message) -> controller.onConnected(message.getPlayerId()));

    	register(MessageType.CATEGORY_LIST, (controller, message) -> controller.onCategoriesReceived(message.getCategories()));

    	register(MessageType.ROOM_UPDATED, (controller, message) -> controller.onRoomUpdated(
                message.getRoomCode(),
                message.getPlayerCount(),
                message.getCategory()
    			));

    	register(MessageType.PHASE_STARTED, (controller, message) -> controller.onPhaseStarted(message));

    	register(MessageType.ROUND_RESULT, (controller, message) -> controller.onRoundResult(message));

    	register(MessageType.GAME_FINISHED, (controller, message) -> controller.onGameFinished(message.getNotice()));

    	register(MessageType.CONTINUE_STATUS, (controller, message) -> controller.onContinueStatus(
                message.getAgreedCount(),
                message.getPlayerCount(),
                message.getNotice()
    			));

    	register(MessageType.ROOM_LEFT, (controller, message) -> controller.onRoomLeft(message.getNotice()));

    	register(MessageType.GAME_ABORTED, (controller, message) -> controller.onGameAborted(message.getNotice()));

        register(MessageType.POST_GAME_OPTIONS,
            (controller, message) -> controller.onPostGameOptions(message.getNotice()));

        register(MessageType.WAITING_FOR_HOST,
            (controller, message) -> controller.onWaitingForHost(message.getNotice()));

    	register(MessageType.ERROR, (controller, message) -> controller.onError(
                message.getErrorCode(),
                message.getNotice()
    			));

    	register(MessageType.TEST_MESSAGE, (controller, message) -> System.out.println("[통신 테스트] " + message.getNotice() ));
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
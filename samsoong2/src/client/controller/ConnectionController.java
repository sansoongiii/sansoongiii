package client.controller;

import java.io.IOException;
import java.util.Objects;

import client.network.GameClient;
import common.Message;
import common.MessageType;

public class ConnectionController {

    private final GameClient gameClient;

    public ConnectionController(GameClient gameClient) {
        this.gameClient = Objects.requireNonNull(gameClient, "GameClient는 null일 수 없습니다.");
    }

    // 서버 연결 요청
    public void connect(String serverIp) throws IOException {
        if (serverIp == null) {
            throw new IllegalArgumentException("서버 IP를 입력해야 합니다.");
        }
        gameClient.connect(serverIp);
    }

    public void disconnect() {
        if (gameClient.isConnected()) {
            gameClient.close();
        }
    }

    public boolean isConnected() {
        return gameClient.isConnected();
    }

    // 방 및 카테고리 요청
    public void requestCategories() throws IOException {
        send(createMessage(MessageType.GET_CATEGORIES));
    }

    public void createRoom() throws IOException {
        send(createMessage(MessageType.CREATE_ROOM));
    }

    public void joinRoom(String roomCode) throws IOException {
        if (roomCode == null || roomCode.isBlank()) {
            throw new IllegalArgumentException("방 코드를 입력해야 합니다.");
        }
        Message message = createMessage(MessageType.JOIN_ROOM);
        message.setRoomCode(roomCode.trim());
        send(message);
    }

    public void selectCategory(String category) throws IOException {
        if (category == null) {
            throw new IllegalArgumentException("카테고리를 선택해야 합니다.");
        }
        Message message = createMessage(MessageType.SELECT_CATEGORY);
        message.setCategory(category);
        send(message);
    }

    // 게임 진행 요청
    public void submitText(int roundNumber, String textDescription) throws IOException {
        if (textDescription == null) {
            throw new IllegalArgumentException("설명 내용을 입력해야 합니다.");
        }
        Message message = createMessage(MessageType.SUBMIT_TEXT);
        message.setRoundNumber(roundNumber);
        message.setTextDescription(textDescription);
        send(message);
    }

    public void submitDrawing(int roundNumber, byte[] drawingData) throws IOException {
        if (drawingData == null) {
            throw new IllegalArgumentException("전송할 그림 데이터가 없습니다.");
        }
        Message message = createMessage(MessageType.SUBMIT_DRAWING);
        message.setRoundNumber(roundNumber);
        message.setDrawingData(drawingData);
        send(message);
    }

    public void submitAnswer(int roundNumber, String submittedAnswer) throws IOException {
        if (submittedAnswer == null) {
            throw new IllegalArgumentException("정답을 입력해야 합니다.");
        }
        Message message = createMessage(MessageType.SUBMIT_ANSWER);
        message.setRoundNumber(roundNumber);
        message.setSubmittedAnswer(submittedAnswer);
        send(message);
    }

    // 라운드 종료 후 연장 요청
    public void respondToContinue(boolean agree) throws IOException {
        Message message = createMessage(MessageType.CONTINUE_RESPONSE);
        message.setAgree(agree);
        send(message);
    }

    // 라운드 종료 후 퇴장 요청
    public void leaveRoom() throws IOException {
        send(createMessage(MessageType.LEAVE_ROOM));
    }

    private Message createMessage(MessageType type) {
        Message message = new Message();
        message.setType(type);
        return message;
    }

    private void send(Message message) throws IOException {
        if (!gameClient.isConnected()) {
            throw new IllegalStateException("서버와 연결되어 있지 않습니다.");
        }
        gameClient.send(message);
    }
}
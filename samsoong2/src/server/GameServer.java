package server;

import common.GameMessageSender;
import common.Message;
import common.MessageType;
import common.Protocol;

import java.util.Map;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.ArrayList;
import java.util.List;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import server.service.GameRoomService;
import server.service.GameService;
import server.service.GameTimerService;
import server.service.GameWordService;
import server.service.RoleAssignmentService;
import server.service.SubmissionValidator;
import server.model.GameRoom;
import server.model.Player;

public class GameServer implements GameMessageSender{
    
    private ServerSocket serverSocket;
    private volatile boolean running = false;

    private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
    private final GameRoomService gameRoomService = new GameRoomService();

    private final GameRoomService gameRoomService = new GameRoomService();
    private final GameService gameService;
	private GameTimerService gameTimerService;
    
    //public GameServer(GameService gameService) {
    //    this.gameService = gameService;
    //}
    
    public GameServer(
            RoleAssignmentService roleAssignmentService,
            GameWordService gameWordService,
            SubmissionValidator submissionValidator,
            GameTimerService gameTimerService) {

        this.gameTimerService = gameTimerService;

        this.gameService = new GameService(
                roleAssignmentService,
                gameWordService,
                submissionValidator,
                gameTimerService,
                this
        );
    }
    
    public void start() throws IOException {
        // ServerSocket 생성
    	serverSocket = new ServerSocket(Protocol.PORT);
    	running = true;
    	System.out.println("서버 시작: 포트 " + Protocol.PORT);

    	while (running) {
	        // accept()로 접속 수락
			Socket socket = serverSocket.accept();
			String playerId = UUID.randomUUID().toString();
			
	        // ClientHandler 생성 및 연결 목록 등록
			ClientHandler handler = new ClientHandler(playerId, socket, this);
			clients.put(playerId, handler);
			
			// 별도 스레드에서 실행
			Thread thread = new Thread(handler);
			thread.start();
			
			System.out.println("클라이언트 접속: " + playerId);
		}     
    }


    public void removeClient(String playerId) {
        ClientHandler removed = clients.remove(playerId);

        if (removed != null) {
            System.out.println(
                    "연결 목록에서 제거: " + playerId
            );
        }
    }
    
    public synchronized void handleMessage(String playerId, Message message) {
        System.out.println("요청 처리: " + message.getType() + " / 참가자: " + playerId);

        switch (message.getType()) {
        case CREATE_ROOM:
            createRoom(playerId);
            break;

        case JOIN_ROOM:
            joinRoom(playerId, message.getRoomCode());
            break;
        
        case GET_CATEGORIES:
            getCategories(playerId);
            break;
            
        case SELECT_CATEGORY:
            selectCategory(playerId, message.getCategory());
            break;

        default:
            sendError(
                    playerId,
                    "INVALID_MESSAGE",
                    "아직 연결하지 않은 요청입니다."
            );
            break;
        }

    }
    
    private void joinRoom(String playerId, String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            sendError(
                    playerId,
                    "INVALID_MESSAGE",
                    "방 코드를 입력해야 합니다."
            );
            return;
        }

        if (gameRoomService.findRoomByPlayerId(playerId) != null) {
            sendError(
                    playerId,
                    "ALREADY_IN_ROOM",
                    "이미 방에 참여하고 있습니다."
            );
            return;
        }

        String code = roomCode.trim();
        GameRoom room = gameRoomService.getRoom(code);

        if (room == null) {
            sendError(
                    playerId,
                    "ROOM_NOT_FOUND",
                    "존재하지 않는 방 코드입니다."
            );
            return;
        }

        try {
            Player player = new Player(playerId);

            // 정원·게임 진행 상태 등의 입장 검증은 A의 코드가 처리
            gameRoomService.joinRoom(code, player);

        } catch (IllegalStateException e) {
            String errorCode = room.getPlayers().size() >= 3
                    ? "ROOM_FULL"
                    : "INVALID_PHASE";

            sendError(playerId, errorCode, e.getMessage());
            return;

        } catch (IllegalArgumentException e) {
            sendError(
                    playerId,
                    "INVALID_MESSAGE",
                    e.getMessage()
            );
            return;
        }

        sendRoomUpdated(room);

        System.out.println(
                "방 입장 완료: " + room.getRoomCode()
                        + " / 인원: " + room.getPlayers().size()
        );
        
        if (room.getPlayers().size() == 3) {
            String hostPlayerId = room.getHostPlayerId();

            System.out.println("3명 입장 완료! 방장에게 카테고리 목록 전송");

            getCategories(hostPlayerId);
        }
    }
    
    private void sendRoomUpdated(GameRoom room) {
        List<Player> players = room.getPlayers();

        List<String> playerIds = new ArrayList<>();

        for (Player player : players) {
            playerIds.add(player.getPlayerId());
        }

        Message response = new Message();
        response.setType(MessageType.ROOM_UPDATED);
        response.setRoomCode(room.getRoomCode());
        response.setPlayerCount(players.size());

        response.setCategory(room.getCategory());

        sendToPlayers(playerIds, response);
    }

    public void stop() {
        // 접속 대기 종료
        // 모든 클라이언트 연결 종료
    }

    @Override
    public void sendTo(String playerId, Message message) {
        ClientHandler handler = clients.get(playerId);

        if (handler == null) {
            System.out.println(
                    "전송 대상이 없습니다: " + playerId
            );
            return;
        }

        handler.send(message);
    }

    @Override
    public void sendToPlayers(Iterable<String> playerIds, Message message) {
        for (String playerId : playerIds) {
            sendTo(playerId, message);
        }
    }
    
    private void sendError(String playerId, String errorCode, String notice) {

        Message response = new Message();
        response.setType(MessageType.ERROR);
        response.setErrorCode(errorCode);
        response.setNotice(notice);

        sendTo(playerId, response);
    }
    
    private void createRoom(String playerId) {
        GameRoom existingRoom = gameRoomService.findRoomByPlayerId(playerId);

        if (existingRoom != null) {
            sendError(playerId, "ALREADY_IN_ROOM", "이미 방에 참여하고 있습니다.");
            return;
        }

        Player player = new Player(playerId);

        // 실제 방 생성과 생성자 입장은 A의 코드가 처리
        GameRoom room = gameRoomService.createRoom(player);

        sendRoomUpdated(room);

        System.out.println("방 생성 완료: " + room.getRoomCode() + " / 인원: " + room.getPlayers().size());
    }
    
    private void getCategories(String playerId) {
    	Message response = new Message();
    	response.setType(MessageType.CATEGORY_LIST);
    	response.setCategories(gameService.getCategories());

    	sendTo(playerId, response);
    }
    private void selectCategory(String playerId, String category) {
        GameRoom room = gameRoomService.findRoomByPlayerId(playerId);

        if (room == null) {
            sendError(playerId, "NOT_IN_ROOM", "참여 중인 방이 없습니다.");
            return;
        }

        if (!playerId.equals(room.getHostPlayerId())) {
            sendError(playerId, "NOT_HOST", "방장만 카테고리를 선택할 수 있습니다.");
            return;
        }

        if (room.getPlayers().size() != 3) {
            sendError(playerId, "INVALID_PLAYER_COUNT", "3명이 모두 입장해야 합니다.");
            return;
        }

        if (category == null || category.isBlank()) {
            sendError(playerId, "INVALID_CATEGORY", "카테고리를 선택해야 합니다.");
            return;
        }

        List<String> categories = gameService.getCategories();

        if (!categories.contains(category)) {
            sendError(playerId, "INVALID_CATEGORY", "존재하지 않는 카테고리입니다.");
            return;
        }

        room.setCategory(category);
        System.out.println(
                "카테고리 선택 완료: " + room.getRoomCode() + " / " + category
        );

        sendRoomUpdated(room);
    }
}
package server.network;

import common.GameMessageSender;
import common.GamePhase;
import common.Message;
import common.MessageType;
import common.Protocol;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import server.service.GameRoomService;
import server.service.GameService;
import server.service.GameTimerService;
import server.service.GameWordService;
import server.service.RoleAssignmentService;
import server.service.SubmissionValidator;
import server.model.GameRoom;
import server.model.GameRound;
import server.model.Player;

public class GameServer implements GameMessageSender {

	private ServerSocket serverSocket;
	private volatile boolean running = false;

	private final Map<String, ClientHandler> clients = new ConcurrentHashMap<>();
	private final GameRoomService gameRoomService = new GameRoomService();
	private final GameService gameService;
	private GameTimerService gameTimerService;

	// public GameServer(GameService gameService) {
	// this.gameService = gameService;
	// }

	public GameServer(RoleAssignmentService roleAssignmentService, GameWordService gameWordService,
			SubmissionValidator submissionValidator, GameTimerService gameTimerService) {

		this.gameTimerService = gameTimerService;

		this.gameService = new GameService(roleAssignmentService, gameWordService, submissionValidator,
				gameTimerService, this);
	}

	public void start() throws IOException {
		// ServerSocket 생성
		serverSocket = new ServerSocket(Protocol.PORT);
		running = true;
		System.out.println("서버 시작: 포트 " + Protocol.PORT);

		while (running) {
			Socket socket;

			try {
				socket = serverSocket.accept();
			} catch (SocketException e) {
				if (!running) {
					break;
				}
				throw e;
			}
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
		GameRoom room = gameRoomService.findRoomByPlayerId(playerId);

		if (room != null) {
			boolean wasHost = playerId.equals(room.getHostPlayerId());

			for (Player player : room.getPlayers()) {
				if (player.getPlayerId().equals(playerId)) {
					player.setConnected(false);
					break;
				}
			}

			if (room.getCurrentRound() != null && !GamePhase.FINISHED.name().equals(room.getPhase())) {
				gameService.abortGame(room, playerId);
				gameRoomService.removeRoom(room.getRoomCode());
			} else if (wasHost) {
				notifyRoomAbortedAndRemove(room, playerId, "방장이 나가 방이 종료되었습니다.");
			} else {
				gameRoomService.leaveRoom(playerId);
				if (!room.getPlayers().isEmpty()) {
					sendRoomUpdated(room);
				}
			}
		}

		ClientHandler removed = clients.remove(playerId);

		if (removed != null) {
			System.out.println("연결 목록에서 제거: " + playerId);
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

		case LEAVE_ROOM:
			leaveRoom(playerId);
			break;

		case GET_CATEGORIES:
			getCategories(playerId);
			break;

		case SELECT_CATEGORY:
			selectCategory(playerId, message.getCategory());
			break;

		case START_GAME:
			startGame(playerId);
			break;

		case SUBMIT_TEXT:
		case SUBMIT_DRAWING:
		case SAVE_DRAWING_PROGRESS:
		case SUBMIT_ANSWER:
		case SAVE_TEXT_PROGRESS:
		case SAVE_ANSWER_PROGRESS:
			handleGameRequest(playerId, message);
			break;

		case CONTINUE_RESPONSE:
			continueResponse(playerId, message);
			break;
		
		case RESTART_GAME:
    		restartGame(playerId);
    		break;
		
		case GAME_FINISHED:
    		endGame(playerId);
    		break;
	
		default:
			sendError(playerId, "INVALID_MESSAGE", "아직 연결하지 않은 요청입니다.");
			break;
		}

	}

	private void joinRoom(String playerId, String roomCode) {
		if (roomCode == null || roomCode.isBlank()) {
			sendError(playerId, "INVALID_MESSAGE", "방 코드를 입력해야 합니다.");
			return;
		}

		if (gameRoomService.findRoomByPlayerId(playerId) != null) {
			sendError(playerId, "ALREADY_IN_ROOM", "이미 방에 참여하고 있습니다.");
			return;
		}

		String code = roomCode.trim();
		GameRoom room = gameRoomService.getRoom(code);

		if (room == null) {
			sendError(playerId, "ROOM_NOT_FOUND", "존재하지 않는 방 코드입니다.");
			return;
		}

		try {
			Player player = new Player(playerId);

			// 정원·게임 진행 상태 등의 입장 검증은 A의 코드가 처리
			gameRoomService.joinRoom(code, player);

		} catch (IllegalStateException e) {
			String errorCode = room.getPlayers().size() >= 3 ? "ROOM_FULL" : "INVALID_PHASE";

			sendError(playerId, errorCode, e.getMessage());
			return;

		} catch (IllegalArgumentException e) {
			sendError(playerId, "INVALID_MESSAGE", e.getMessage());
			return;
		}

		sendRoomUpdated(room);

		System.out.println("방 입장 완료: " + room.getRoomCode() + " / 인원: " + room.getPlayers().size());

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
		running = false;

		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				System.out.println("서버 소켓 종료 오류: " + e.getMessage());
			}
		}

		for (ClientHandler handler : new ArrayList<>(clients.values())) {

			handler.close();
		}

		gameTimerService.close();

		System.out.println("서버 종료 완료");
	}

	@Override
	public void sendTo(String playerId, Message message) {
		ClientHandler handler = clients.get(playerId);

		if (handler == null) {
			System.out.println("전송 대상이 없습니다: " + playerId);
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
		System.out.println("카테고리 선택 완료: " + room.getRoomCode() + " / " + category);

		sendRoomUpdated(room);
	}

	private void startGame(String playerId) {
		GameRoom room = gameRoomService.findRoomByPlayerId(playerId);

		if (room == null) {
			sendError(playerId, "NOT_IN_ROOM", "참여 중인 방이 없습니다.");
			return;
		}

		if (!playerId.equals(room.getHostPlayerId())) {
			sendError(playerId, "NOT_HOST", "방장만 게임을 시작할 수 있습니다.");
			return;
		}

		try {
			GameRound round = gameService.startGame(room, room.getCategory());

			System.out.println("게임 시작: " + room.getRoomCode() + " / 카테고리: " + room.getCategory() + " / 선택된 제시어: "
					+ round.getAnswer());

		} catch (IllegalStateException e) {
		    if (e.getMessage() != null && e.getMessage().contains("사용하지 않은 제시어")) {
		        notifyRoomAbortedAndRemove(
		            room,
        		    "",
           			"사용 가능한 제시어가 없어 게임이 종료되었습니다."
        		);
	        	return;
    		}
			sendError(playerId, "START_FAILED", e.getMessage());
		} catch (IllegalArgumentException e) {
    		sendError(playerId, "START_FAILED", e.getMessage());
		}
	}
	
	private void leaveRoom(String playerId) {
		GameRoom room = gameRoomService.findRoomByPlayerId(playerId);

		if (room == null) {
			sendError(playerId, "NOT_IN_ROOM", "참여 중인 방이 없습니다.");
			return;
		}

		boolean wasHost = playerId.equals(room.getHostPlayerId());

		if (wasHost && room.getCurrentRound() == null) {
			notifyRoomAbortedAndRemove(room, playerId, "방장이 나가 방이 종료되었습니다.");

			Message leftMsg = new Message();
			leftMsg.setType(MessageType.ROOM_LEFT);
			leftMsg.setNotice("방에서 나갔습니다.");
			sendTo(playerId, leftMsg);
			return;
		}

		gameRoomService.leaveRoom(playerId);

		Message leftMsg = new Message();
		leftMsg.setType(MessageType.ROOM_LEFT);
		leftMsg.setNotice("방에서 나갔습니다.");
		sendTo(playerId, leftMsg);

		if (!room.getPlayers().isEmpty()) {
			sendRoomUpdated(room);
		}

		System.out.println("방 퇴장 완료: " + room.getRoomCode() + " / 남은 인원: " + room.getPlayers().size());
	}

	private void notifyRoomAbortedAndRemove(GameRoom room, String excludePlayerId, String notice) {
		List<String> targets = new ArrayList<>();
		for (Player player : room.getPlayers()) {
			if (!player.getPlayerId().equals(excludePlayerId) && player.isConnected()) {
				targets.add(player.getPlayerId());
			}
		}

		gameRoomService.removeRoom(room.getRoomCode());

		Message abortedMsg = new Message();
		abortedMsg.setType(MessageType.GAME_ABORTED);
		abortedMsg.setNotice(notice);
		sendToPlayers(targets, abortedMsg);
	}

	private void handleGameRequest(String playerId, Message message) {

		GameRoom room = gameRoomService.findRoomByPlayerId(playerId);

		if (room == null) {
			sendError(playerId, "NOT_IN_ROOM", "참여 중인 방이 없습니다.");
			return;
		}

		// GameService 타이머 처리와 같은 잠금 사용
		synchronized (gameService) {
			GameRound round = room.getCurrentRound();

			if (round == null) {
				sendError(playerId, "INVALID_PHASE", "진행 중인 라운드가 없습니다.");
				return;
			}

			if (message.getRoundNumber() != round.getRoundNumber()) {

				sendError(playerId, "INVALID_ROUND", "현재 라운드와 다른 요청입니다.");
				return;
			}

			try {
				switch (message.getType()) {
				case SUBMIT_TEXT:
					gameService.submitText(room, playerId, message.getTextDescription());
					System.out.println("[설명 제출] 방: " + room.getRoomCode());
					break;

				case SUBMIT_DRAWING:
					gameService.submitDrawing(room, playerId, message.getDrawingData());
					System.out.println("[그림 제출] 방: " + room.getRoomCode());
					break;

				case SUBMIT_ANSWER:
					gameService.submitAnswer(room, playerId, message.getSubmittedAnswer());
					System.out.println("[정답 제출] 방: " + room.getRoomCode());
					break;

				case SAVE_TEXT_PROGRESS:
					gameService.saveTextProgress(room, playerId, message.getTextDescription());
					break;

				case SAVE_DRAWING_PROGRESS:
					gameService.saveDrawingProgress(room, playerId, message.getDrawingData());
					break;

				case SAVE_ANSWER_PROGRESS:
					gameService.saveAnswerProgress(room, playerId, message.getSubmittedAnswer());
					break;

				default:
					throw new IllegalArgumentException("지원하지 않는 게임 요청입니다.");
				}

			} catch (SecurityException e) {
				sendError(playerId, "INVALID_ROLE", e.getMessage());

			} catch (IllegalStateException e) {
				if (isProgressRequest(message.getType())) {
					// 단계가 바뀐 뒤 도착한 중간 저장은 반영하지 않음
					System.out.println("[중간 저장 거절] " + e.getMessage());
				} else {
					sendError(playerId, "INVALID_PHASE", e.getMessage());
				}

			} catch (IllegalArgumentException e) {
				String errorCode = message.getType() == MessageType.SUBMIT_TEXT ? "TEXT_VALIDATION_FAILED"
						: "INVALID_MESSAGE";

				sendError(playerId, errorCode, e.getMessage());
			}
		}
	}

	private boolean isProgressRequest(MessageType type) {
		return type == MessageType.SAVE_TEXT_PROGRESS || type == MessageType.SAVE_DRAWING_PROGRESS
				|| type == MessageType.SAVE_ANSWER_PROGRESS;
	}

	private void continueResponse(String playerId, Message message) {

		if (!message.isAgree()) {
			return;
		}

		GameRoom room = gameRoomService.findRoomByPlayerId(playerId);

		if (room == null) {
			sendError(playerId, "NOT_IN_ROOM", "참여 중인 방이 없습니다.");
			return;
		}

		synchronized (gameService) {
			try {
				int agreedCount = gameService.markPlayerReady(room, playerId);

				List<String> playerIds = new ArrayList<>();

				for (Player player : room.getPlayers()) {
					playerIds.add(player.getPlayerId());
				}

				Message status = new Message();
				status.setType(MessageType.CONTINUE_STATUS);
				status.setRoundNumber(room.getCurrentRound().getRoundNumber());
				status.setAgreedCount(agreedCount);
				status.setPlayerCount(playerIds.size());
				status.setNotice("다음 라운드 진행 확인 중");

				sendToPlayers(playerIds, status);

				if (agreedCount == playerIds.size()) {
					GameRound nextRound = gameService.startNextRound(room);

					if (nextRound == null) {
						String hostPlayerId = room.getHostPlayerId();

					    // 방장에게 재시작 / 종료 선택 메시지
    					Message options = new Message();
    					options.setType(MessageType.POST_GAME_OPTIONS);
    					options.setNotice("모든 라운드가 종료되었습니다. 다음 행동을 선택하세요.");

					    sendTo(hostPlayerId, options);

					    // 나머지 플레이어에게 대기 메시지
					    Message waiting = new Message();
					    waiting.setType(MessageType.WAITING_FOR_HOST);
    					waiting.setNotice("방장이 다음 게임 진행 여부를 선택하고 있습니다.");

					    for (Player player : room.getPlayers()) {
					        if (!player.getPlayerId().equals(hostPlayerId)) {
            					sendTo(player.getPlayerId(), waiting);
        					}
    					}
					}
				}

			} catch (IllegalArgumentException | IllegalStateException e) {

				sendError(playerId, "CONTINUE_FAILED", e.getMessage());
			}
		}

	}

	private void restartGame(String playerId) {
	    GameRoom room = gameRoomService.findRoomByPlayerId(playerId);

    	if (room == null) {
        	sendError(playerId, "NOT_IN_ROOM", "참여 중인 방이 없습니다.");
        	return;
    	}
		synchronized (gameService) {
        	// 방장만 재시작 선택 가능
        	if (!playerId.equals(room.getHostPlayerId())) {
            	sendError(playerId, "NOT_HOST", "방장만 재시작할 수 있습니다.");
            	return;
        	}

	        // 모든 라운드가 끝난 뒤에만 재시작 가능
    	    if (!GamePhase.FINISHED.name().equals(room.getPhase())) {
        	    sendError(playerId, "INVALID_PHASE", "게임이 모두 종료된 후 재시작할 수 있습니다.");
            	return;
        	}

	        try {
    	        // 기존 방 상태 초기화
        	    room.resetForRestart();

	            System.out.println("게임 재시작 준비: " + room.getRoomCode());

	            // 방장에게만 카테고리 목록 전송
    	        getCategories(playerId);

        	    // 기존 방과 플레이어 정보 유지
            	sendRoomUpdated(room);

        	} catch (IllegalStateException e) {
            	sendError(playerId, "RESTART_FAILED", e.getMessage());
        	}
		}
	}

	private void endGame(String playerId) {
    	GameRoom room = gameRoomService.findRoomByPlayerId(playerId);

	    if (room == null) {
    	    sendError(playerId, "NOT_IN_ROOM", "참여 중인 방이 없습니다.");
        	return;
    	}

    	synchronized (gameService) {
    	    if (!playerId.equals(room.getHostPlayerId())) {
            	sendError(playerId, "NOT_HOST", "방장만 게임을 종료할 수 있습니다.");
            	return;
        	}

	        if (!GamePhase.FINISHED.name().equals(room.getPhase())) {
    	        sendError(playerId, "INVALID_PHASE", "모든 라운드가 종료된 후 게임을 종료할 수 있습니다.");
        	    return;
        	}

	        // 남아 있는 타이머 취소
    	    gameService.cancelRoomTimer(room);

        	// 모든 플레이어에게 종료 알림
        	Message finished = new Message();
        	finished.setType(MessageType.GAME_FINISHED);
        	finished.setNotice("참가자의 선택으로 게임이 종료되었습니다.");

	        List<String> playerIds = new ArrayList<>();

    	    for (Player player : room.getPlayers()) {
        	    playerIds.add(player.getPlayerId());
        	}

	        sendToPlayers(playerIds, finished);

    	    // 방 제거
        	gameRoomService.removeRoom(room.getRoomCode());

        	// 모든 플레이어 연결 종료
        	for (String id : playerIds) {
            	ClientHandler handler = clients.remove(id);
            	if (handler != null) {
                	handler.close();
            	}
        	}
        	System.out.println("게임 종료 및 모든 플레이어 연결 해제: " + room.getRoomCode());
    	}
	}
}
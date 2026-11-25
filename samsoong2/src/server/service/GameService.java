package server.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import common.GameMessageSender;
import common.GamePhase;
import common.Message;
import common.MessageType;
import common.PlayerRole;
import server.model.GameRoom;
import server.model.GameRound;
import server.model.Player;
import server.model.RoundResult;

public class GameService {
	private static final int PHASE_TIME_SECONDS = 30;

    private final RoleAssignmentService roleAssignmentService;
    private final GameWordService gameWordService;
    private final SubmissionValidator submissionValidator;
    private final GameTimerService gameTimerService;
    private final GameMessageSender gameMessageSender;
    
    public GameService(
            RoleAssignmentService roleAssignmentService,
            GameWordService gameWordService,
            SubmissionValidator submissionValidator,
            GameTimerService gameTimerService,
            GameMessageSender gameMessageSender) {

        this.roleAssignmentService = Objects.requireNonNull(roleAssignmentService);
        this.gameWordService = Objects.requireNonNull(gameWordService);
        this.submissionValidator = Objects.requireNonNull(submissionValidator);
        this.gameTimerService = Objects.requireNonNull(gameTimerService);
        this.gameMessageSender = Objects.requireNonNull(gameMessageSender);
    }

    public List<String> getCategories() {
        return gameWordService.getCategories();
    }
    
    public synchronized GameRound startGame(GameRoom room,String category) {
        if (!room.canStart()) {
            throw new IllegalStateException("플레이어 3명이 모두 입장해야 게임을 시작할 수 있습니다.");
        }
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("카테고리를 정해야 시작할 수 있습니다.");
        }
        
        return createRound(room, 1, category);
    }
    private GameRound createRound(GameRoom room,int roundNumber,String category) {
        Map<String, String> assignments =roleAssignmentService.assignRoles(room);
        //var assignments = roleAssignmentService.assignRoles(room);
        List<String> words = new ArrayList<>(gameWordService.getWords(category));

        Collections.shuffle(words);

        String answer = null;

        for (String word : words) {
            if (room.usedAnswer(word)) {
                answer = word;
                break;
            }
        }

        if (answer == null) {
            throw new IllegalStateException(
                    "해당 카테고리에 아직 사용하지 않은 제시어가 없습니다."
            );
        }

        GameRound round = new GameRound(
            roundNumber,category,answer,assignments
        );

        room.setCurrentRound(round);
        room.setPhase(GamePhase.TEXT_WRITING.name());

        startPhaseTimer(room, round, GamePhase.TEXT_WRITING.name());
        sendPhaseStarted(room, round, GamePhase.TEXT_WRITING.name());

        return round;
    }
    public synchronized GameRound startNextRound(GameRoom room) {

        GameRound previousRound = getCurrentRound(room);

        submissionValidator.validatePhase(
            previousRound,GamePhase.ROUND_RESULT.name()
        );

        int nextRoundNumber = previousRound.getRoundNumber() + 1;

        if (nextRoundNumber > 3) {
            previousRound.setPhase(GamePhase.FINISHED.name());
            room.setPhase(GamePhase.FINISHED.name());
            return null;
        }

        return createRound(
            room,nextRoundNumber,previousRound.getCategory()
        );
    }
    
    public synchronized int markPlayerReady(GameRoom room, String playerId) {
        GameRound round = getCurrentRound(room);

        submissionValidator.validatePhase(
            round,
            GamePhase.ROUND_RESULT.name()
        );

        round.markPlayerReady(playerId);

        return round.areAllPlayersReady()
                ? room.getPlayers().size()
                : getReadyCount(round);
    }

    private int getReadyCount(GameRound round) {
        return round.getReadyCount();
    }

    public synchronized void submitText(GameRoom room,String playerId,String textDescription) {

        GameRound round = getCurrentRound(room);

        // 1. 제출 권한 검사
        submissionValidator.validateRole(
            round, playerId, PlayerRole.TEXT_WRITER.name()
        );

        // 2. 제출 가능한 단계인지 검사
        submissionValidator.validatePhase(
            round, GamePhase.TEXT_WRITING.name()
        );

        // 3. 설명 내용 검사
        submissionValidator.validateDescription(
            round, textDescription
        );

        // 4. 설명 저장 및 다음 단계로 이동
        round.submitText(textDescription);
        changePhase(room, round, GamePhase.DRAWING.name());
    }
    
    public synchronized void submitDrawing(GameRoom room,String playerId,byte[] drawingData) {
        GameRound round = getCurrentRound(room);

        // 검증
        submissionValidator.validateRole(
            round, playerId, PlayerRole.DRAWER.name()
        );

        submissionValidator.validatePhase(
            round, GamePhase.DRAWING.name()
        );

        submissionValidator.validateDrawing(drawingData);

        // 그림 저장 및 다음 단계로 이동
        round.submitDrawing(drawingData);
        changePhase(room, round, GamePhase.ANSWERING.name());
    }
    public synchronized void submitAnswer(GameRoom room,String playerId,String submittedAnswer) {
        GameRound round = getCurrentRound(room);

        // 검증
        submissionValidator.validateRole(
            round, playerId, PlayerRole.ANSWERER.name()
        );

        submissionValidator.validatePhase(
            round, GamePhase.ANSWERING.name()
        );

        submissionValidator.validateAnswer(submittedAnswer);

        // 정답 저장 및 결과 단계로 이동
        round.submitAnswer(submittedAnswer);
        changePhase(room, round, GamePhase.ROUND_RESULT.name());
        sendRoundResult(room);
    }
    
    public synchronized void saveTextProgress(GameRoom room,String playerId,String textDescription) {
        GameRound round = getCurrentRound(room);
        submissionValidator.validateRole(
                round,playerId,PlayerRole.TEXT_WRITER.name()
        );
        submissionValidator.validatePhase(
                round,GamePhase.TEXT_WRITING.name()
        );

        round.updateTextDescription(textDescription);
    }
    
    public synchronized void saveDrawingProgress(GameRoom room,String playerId,byte[] drawingData) {
        GameRound round = getCurrentRound(room);

        submissionValidator.validateRole(
                round,playerId,PlayerRole.DRAWER.name()
        );

        submissionValidator.validatePhase(
                round,GamePhase.DRAWING.name()
        );

        round.updateDrawingData(drawingData);
    }
    
    public synchronized void saveAnswerProgress(GameRoom room,String playerId,String submittedAnswer) {
        GameRound round = getCurrentRound(room);

        submissionValidator.validateRole(
                round,playerId,PlayerRole.ANSWERER.name()
        );

        submissionValidator.validatePhase(
                round,GamePhase.ANSWERING.name()
        );

        round.updateSubmittedAnswer(submittedAnswer);
    }
    
    public RoundResult createRoundResult(GameRoom room) {
        GameRound round = getCurrentRound(room);

        submissionValidator.validatePhase(
            round, GamePhase.ROUND_RESULT.name()
        );

        boolean correct =round.matchesAnswer(round.getSubmittedAnswer());

        return new RoundResult(
            round.getRoundNumber(),
            getAnswerForResult(round),
            round.getTextDescription(),
            round.getDrawingData(),
            round.getSubmittedAnswer(),
            correct
        );
    }
    private void sendRoundResult(GameRoom room) {
        RoundResult result = createRoundResult(room);

        for (Player player : room.getPlayers()) {
            Message message = new Message();

            message.setType(MessageType.ROUND_RESULT);
            message.setRoundNumber(result.getRoundNumber());
            message.setAnswer(result.getAnswer());
            message.setTextDescription(result.getTextDescription());
            message.setDrawingData(result.getDrawingData());
            message.setSubmittedAnswer(result.getSubmittedAnswer());
            message.setCorrect(result.isCorrect());

            gameMessageSender.sendTo(player.getPlayerId(), message);
        }
    }
    private void changePhase(GameRoom room,GameRound round,String nextPhase) {

        String currentPhase = round.getPhase();

        // 이전 단계의 타이머 취소
        gameTimerService.cancelTimer(getTimerId(room, round, currentPhase));

        round.setPhase(nextPhase);
        room.setPhase(nextPhase);

        // 결과 단계에는 다음 단계 타이머가 필요하지 않음
        if (!GamePhase.ROUND_RESULT.name().equals(nextPhase)&& !GamePhase.FINISHED.name().equals(nextPhase)) {
        	startPhaseTimer(room, round, nextPhase);
        	sendPhaseStarted(room, round, nextPhase);
        }
    }

    // 단계별 타이머 시작
    private void startPhaseTimer(GameRoom room,GameRound round,String phase) {
        String timerId = getTimerId(room, round, phase);

        gameTimerService.startTimer(
            timerId,PHASE_TIME_SECONDS,() -> handleTimeout(room, round, phase)
        );
    }

    // 시간 초과 처리
    private synchronized void handleTimeout( GameRoom room,GameRound round,String expectedPhase) {

        //타이머가 실행되기 전에 이미 단계가 바뀌었거나
        //다른 라운드로 넘어갔다면 아무 작업도 하지 않음.
        if (room.getCurrentRound() != round
                || !expectedPhase.equals(round.getPhase())
                || !expectedPhase.equals(room.getPhase())) {
            return;
        }

        switch (expectedPhase) {
        	case "TEXT_WRITING":
        		String description = round.getTextDescription();
        		if (description == null || description.isBlank()) {
        	        description = "아무 설명도 작성하지 못했습니다.";
        	    }
        	    if (!round.isTextSubmitted()) {
        	        round.submitText(description);
        	    }
        	    changePhase(room, round, GamePhase.DRAWING.name());
        	    break;
        	case "DRAWING":
        		byte[] drawingData = round.getDrawingData();

        		if (!round.isDrawingSubmitted() && drawingData != null&& drawingData.length > 0) {
        		        round.submitDrawing(drawingData);
        		}

        	    changePhase(room, round, GamePhase.ANSWERING.name());
        	    break;
        	case "ANSWERING":
        	    String submittedAnswer = round.getSubmittedAnswer();
        	    if (submittedAnswer == null || submittedAnswer.isBlank()) {
        	        submittedAnswer = "정답을 작성하지 못했습니다.";
        	    }
        	    if (!round.isAnswerSubmitted()) {
        	        round.submitAnswer(submittedAnswer);
        	    }

        	    changePhase(room, round, GamePhase.ROUND_RESULT.name());
        	    sendRoundResult(room);
        	    break;
        	default:
        		break;
        }
    }

    // 방과 라운드와 단계별로 타이머 ID 구분
    private String getTimerId(GameRoom room,GameRound round,String phase) {

        return System.identityHashCode(room)
            + "-" + round.getRoundNumber()
            + "-" + phase;
    }

    public void cancelRoomTimer(GameRoom room) {
        GameRound round = room.getCurrentRound();

        if (round == null) {
            return;
        }

        gameTimerService.cancelTimer(
            getTimerId(room, round, round.getPhase())
        );
    }
    
 // 플레이어 연결 종료로 게임 중단
    public synchronized void abortGame(GameRoom room, String disconnectedPlayerId) {

        cancelRoomTimer(room);

        room.setPhase(GamePhase.FINISHED.name());

        GameRound round = room.getCurrentRound();

        if (round != null) {
            round.setPhase(GamePhase.FINISHED.name());
        }

        for (Player player : room.getPlayers()) {

            // 연결이 끊긴 플레이어에게는 전송하지 않음
            if (player.getPlayerId().equals(disconnectedPlayerId)) {
                continue;
            }

            if (!player.isConnected()) {
                continue;
            }

            Message message = new Message();
            message.setType(MessageType.GAME_ABORTED);
            message.setNotice("다른 플레이어의 연결이 끊겨 게임이 중단되었습니다.");

            gameMessageSender.sendTo(player.getPlayerId(), message);
        }
    }
    
    private GameRound getCurrentRound(GameRoom room) {

        GameRound round = room.getCurrentRound();

        if (round == null) {
            throw new IllegalStateException("현재 진행 중인 라운드가 없습니다.");
        }

        return round;
    }
    private String getAnswerForResult(GameRound round) {
        return round.getAssignments().entrySet()
            .stream()
            .filter(entry ->
                PlayerRole.TEXT_WRITER.name().equals(entry.getValue()))
            .map(entry -> round.getAnswerFor(entry.getKey()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "설명 작성자를 찾을 수 없습니다."
            ));
    }
    
    private void sendPhaseStarted(GameRoom room, GameRound round, String phase) {

        for (Player player : room.getPlayers()) {

            String playerId = player.getPlayerId();
            String role = round.getAssignments().get(playerId);

            Message message = new Message();
            message.setType(MessageType.PHASE_STARTED);
            message.setRoundNumber(round.getRoundNumber());
            message.setPhase(phase);
            message.setRole(role);

            // 제한 시간 종료 시각
            message.setDeadlineEpochMillis(
                System.currentTimeMillis() + PHASE_TIME_SECONDS * 1000L
            );

            // 설명 작성자에게만 제시어 전달
            if (GamePhase.TEXT_WRITING.name().equals(phase)
                    && PlayerRole.TEXT_WRITER.name().equals(role)) {
                message.setAnswer(round.getAnswerFor(playerId));
            }

            // DRAWER에게 Writer의 설명 전달
            if (GamePhase.ANSWERING.name().equals(phase)
                    && PlayerRole.ANSWERER.name().equals(role)) {

                message.setTextDescription(round.getTextDescription());
            }

            // ANSWERER에게 그림 전달
            if (GamePhase.ANSWERING.name().equals(phase)
                    && PlayerRole.ANSWERER.name().equals(role)) {
                message.setDrawingData(round.getDrawingData());
            }

            gameMessageSender.sendTo(playerId, message);
        }
    }
}

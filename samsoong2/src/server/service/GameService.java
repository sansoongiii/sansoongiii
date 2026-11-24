package server.service;

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
    
    public GameRound startGame(GameRoom room,String category) {
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
        String answer = gameWordService.getRandomWord(category);

        GameRound round = new GameRound(
            roundNumber,category,answer,assignments
        );

        room.setCurrentRound(round);
        room.setPhase(GamePhase.TEXT_WRITING.name());

        startPhaseTimer(room, round, GamePhase.TEXT_WRITING.name());
        sendPhaseStarted(room, round, GamePhase.TEXT_WRITING.name());

        return round;
    }
    public GameRound startNextRound(GameRoom room) {

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

    public void submitText(GameRoom room,String playerId,String textDescription) {

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
    
    public void submitDrawing(GameRoom room,String playerId,byte[] drawingData) {
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
    public void submitAnswer(GameRoom room,String playerId,String submittedAnswer) {
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
    }
    
    public void saveTextProgress(GameRoom room,String playerId,String textDescription) {
        GameRound round = getCurrentRound(room);
        submissionValidator.validateRole(
                round,playerId,PlayerRole.TEXT_WRITER.name()
        );
        submissionValidator.validatePhase(
                round,GamePhase.TEXT_WRITING.name()
        );

        round.updateTextDescription(textDescription);
    }
    
    public void saveDrawingProgress(GameRoom room,String playerId,byte[] drawingData) {
        GameRound round = getCurrentRound(room);

        submissionValidator.validateRole(
                round,playerId,PlayerRole.DRAWER.name()
        );

        submissionValidator.validatePhase(
                round,GamePhase.DRAWING.name()
        );

        round.updateDrawingData(drawingData);
    }
    
    public void saveAnswerProgress(GameRoom room,String playerId,String submittedAnswer) {
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
    private void changePhase(GameRoom room,GameRound round,String nextPhase) {

        String currentPhase = round.getPhase();

        // 이전 단계의 타이머 취소
        gameTimerService.cancelTimer(getTimerId(room, round, currentPhase));

        round.setPhase(nextPhase);
        room.setPhase(nextPhase);

        // 결과 단계에는 다음 단계 타이머가 필요하지 않음
        if (!GamePhase.ROUND_RESULT.equals(nextPhase)&& !GamePhase.FINISHED.equals(nextPhase)) {
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
    private void handleTimeout( GameRoom room,GameRound round,String expectedPhase) {

        //타이머가 실행되기 전에 이미 단계가 바뀌었거나
        //다른 라운드로 넘어갔다면 아무 작업도 하지 않음.
        if (room.getCurrentRound() != round
                || !expectedPhase.equals(round.getPhase())
                || !expectedPhase.equals(room.getPhase())) {
            return;
        }

        switch (expectedPhase) {
        	case "TEXT_WRITING":
        		changePhase(room,round,GamePhase.DRAWING.name());
        		break;
        	case "DRAWING":
        		changePhase(room,round,GamePhase.ANSWERING.name());
        		break;
        	case "ANSWERING":
        		changePhase(room,round,GamePhase.ROUND_RESULT.name());
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
                PlayerRole.TEXT_WRITER.equals(entry.getValue()))
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
            if (PlayerRole.TEXT_WRITER.name().equals(role)) {
                message.setAnswer(round.getAnswerFor(playerId));
            }

            // DRAWER에게 Writer의 설명 전달
            if (PlayerRole.DRAWER.name().equals(role)) {
                message.setTextDescription(round.getTextDescription());
            }

            // ANSWERER에게 그림 전달
            if (PlayerRole.ANSWERER.name().equals(role)) {
                message.setDrawingData(round.getDrawingData());
            }

            gameMessageSender.sendTo(playerId, message);
        }
    }
}

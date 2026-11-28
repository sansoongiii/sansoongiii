package server.model;

import java.util.ArrayList;
import java.util.List;
import java.util.HashSet;
import java.util.Set;
import common.GamePhase;

public class GameRoom {

    private static final int REQUIRED_PLAYERS = 3;

    private final String roomCode;
    private final List<Player> players = new ArrayList<>();

    private GameRound currentRound;
    private String phase = GamePhase.WAITING.name();

    private String category = "";
    private final Set<String> usedAnswers = new HashSet<>();
    
    private final Set<String> restartVotes = new HashSet<>();

    public GameRoom(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            throw new IllegalArgumentException("방 코드는 필수입니다.");
        }
        this.roomCode = roomCode;
    }

    public void addPlayer(Player player) {
    	if (player == null) {
            throw new IllegalArgumentException("플레이어 정보는 필수입니다.");
        }
        if (!GamePhase.WAITING.name().equals(phase)) {
            throw new IllegalStateException("게임이 시작된 방에는 입장할 수 없습니다.");
        }
        if (players.size() >= REQUIRED_PLAYERS) {
            throw new IllegalStateException("게임방 인원이 가득 찼습니다.");
        }
        boolean alreadyJoined = players.stream().anyMatch(p -> p.getPlayerId().equals(player.getPlayerId()));
        if (alreadyJoined) {
            throw new IllegalArgumentException("이미 입장한 플레이어입니다.");
        }

        players.add(player);
    }
    
    public String getHostPlayerId() {
        if (players.isEmpty()) {
            return null;
        }

        // 첫 번째 참가자가 방장
        return players.get(0).getPlayerId();
    }

    public boolean canStart() {
        return GamePhase.WAITING.name().equals(phase)&& players.size() == REQUIRED_PLAYERS&& players.stream().allMatch(Player::isConnected);
    }

    public String getRoomCode() {
        return roomCode;
    }

    public List<Player> getPlayers() {
        return List.copyOf(players);
    }

    public GameRound getCurrentRound() {
        return currentRound;
    }

    public void setCurrentRound(GameRound currentRound) {
        this.currentRound = currentRound;
    }

    public String getPhase() {
        return phase;
    }

    public void setPhase(String phase) {
        this.phase = phase;
    }
    
    public String getCategory() {
        return category;
    }
    public void setCategory(String category) {
        if (category == null || category.isBlank()) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }

        this.category = category;
    }
    
    public synchronized boolean usedAnswer(String answer) {
        if (answer == null || answer.isBlank()) {
            throw new IllegalArgumentException("제시어는 필수입니다.");
        }

        return usedAnswers.add(answer);
    }

    // 재시작 투표 등록 (중복 투표 방지)
    public synchronized boolean voteRestart(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("플레이어 ID는 필수입니다.");
        }

        boolean isPlayer = players.stream()
            .anyMatch(player -> player.getPlayerId().equals(playerId));

        if (!isPlayer) {
        throw new IllegalArgumentException("방에 참여하지 않은 플레이어입니다.");
        }

        return restartVotes.add(playerId);
    }

    // 현재 재시작 동의 인원    
    public synchronized int getRestartVoteCount() {
        return restartVotes.size();
    }

    // 모든 플레이어가 재시작에 동의했는지 확인
    public synchronized boolean allPlayersVotedRestart() {
        return restartVotes.size() == players.size()
            && !players.isEmpty();
    }

    public synchronized void resetForRestart() {
        if (!GamePhase.FINISHED.name().equals(phase) && !GamePhase.ROUND_RESULT.name().equals(phase)) {
            throw new IllegalStateException("게임 결과 화면에서만 재시작할 수 있습니다.");
        }

        currentRound = null;
        phase = GamePhase.WAITING.name();
        category = "";

        restartVotes.clear();
    }
}
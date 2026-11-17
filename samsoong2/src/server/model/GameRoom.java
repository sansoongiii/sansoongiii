package server.model;

import java.util.ArrayList;
import java.util.List;

import common.GamePhase;

public class GameRoom {

    private static final int REQUIRED_PLAYERS = 3;

    private final String roomCode;
    private final List<Player> players = new ArrayList<>();

    private GameRound currentRound;
    private String phase = GamePhase.WAITING.name();

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

    public boolean canStart() {
        return phase == GamePhase.WAITING.name()&& players.size() == REQUIRED_PLAYERS&& players.stream().allMatch(Player::isConnected);
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
}
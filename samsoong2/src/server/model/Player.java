package server.model;

public class Player {

    private final String playerId;
    private String role;
    private boolean connected;

    public Player(String playerId, String name) {
    	if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("플레이어 ID는 필수입니다.");
        }
        this.playerId = playerId;
        this.connected = true;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getRole() {
        return role;
    }

    public void assignRole(String role) {
        if (role == null) {
            throw new IllegalArgumentException("플레이어 역할은 필수입니다.");
        }
        this.role = role;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
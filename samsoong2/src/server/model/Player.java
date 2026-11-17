package server.model;

public class Player {

    private final String playerId;
    private String name;
    private String role;
    private boolean connected;

    public Player(String playerId, String name) {
    	if (playerId == null || playerId.isBlank()) {
            throw new IllegalArgumentException("플레이어 ID는 필수입니다.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("플레이어 이름은 필수입니다.");
        }
        this.playerId = playerId;
        this.name = name;
        this.connected = true;
    }

    public String getPlayerId() {
        return playerId;
    }

    public String getName() {
        return name;
    }

    public String getRole() {
        return role;
    }

    public void changeName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("플레이어 이름은 비어 있을 수 없습니다.");
        }
        this.name = name;
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
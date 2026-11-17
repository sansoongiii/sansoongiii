package server.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import server.model.GameRoom;
import server.model.Player;

public class GameRoomService {

    private final Map<String, GameRoom> rooms = new HashMap<>();

    //새로운 게임방을 생성하고 생성자를 입장시킴
    public synchronized GameRoom createRoom(Player player) {
        Objects.requireNonNull(player, "플레이어 정보는 필수입니다.");

        String roomCode;
        do {
            roomCode = generateRoomCode();
        } while (rooms.containsKey(roomCode));

        GameRoom room = new GameRoom(roomCode);
        room.addPlayer(player);

        rooms.put(roomCode, room);

        return room;
    }

    //기존 게임방에 플레이어 입장
    public synchronized GameRoom joinRoom(String roomCode, Player player) {
        if (roomCode == null || roomCode.isBlank()) {
            throw new IllegalArgumentException("방 코드는 필수입니다.");
        }

        Objects.requireNonNull(player, "플레이어 정보는 필수입니다.");

        GameRoom room = rooms.get(roomCode);

        if (room == null) {
            throw new IllegalArgumentException("존재하지 않는 게임방입니다.");
        }

        room.addPlayer(player);

        return room;
    }

    //방 코드로 게임방 조회
    public synchronized GameRoom getRoom(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            return null;
        }

        return rooms.get(roomCode);
    }

    //플레이어 ID로 해당 플레이어가 속한 게임방 조회
    public synchronized GameRoom findRoomByPlayerId(String playerId) {
        if (playerId == null || playerId.isBlank()) {
            return null;
        }

        for (GameRoom room : rooms.values()) {
            boolean found = room.getPlayers().stream()
                    .anyMatch(player -> player.getPlayerId().equals(playerId));

            if (found) {
                return room;
            }
        }

        return null;
    }

    //방코드로 게임방 없애기
    public synchronized boolean removeRoom(String roomCode) {
        if (roomCode == null || roomCode.isBlank()) {
            return false;
        }

        return rooms.remove(roomCode) != null;
    }

    //게임방 수
    public synchronized int getRoomCount() {
        return rooms.size();
    }

    //6자리 방코드 생성
    private String generateRoomCode() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 6).toUpperCase();
    }
}
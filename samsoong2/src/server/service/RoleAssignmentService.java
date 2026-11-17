package server.service;

import server.model.GameRoom;
import server.model.Player;
import common.PlayerRole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RoleAssignmentService {
    public Map<String, String> assignRoles(GameRoom room) {
        List<Player> players = new ArrayList<>(room.getPlayers());
        if (players.size() != 3) {
            throw new IllegalStateException("역할 배정에는 플레이어 3명이 필요합니다.");
        }

        Collections.shuffle(players);

        String[] roles = {
        	PlayerRole.TEXT_WRITER.name(),
        	PlayerRole.DRAWER.name(),
        	PlayerRole.ANSWERER.name()
        };

        Map<String, String> assignments=new HashMap<>();
        
        for (int i = 0; i < roles.length; i++) {
            Player player = players.get(i);
            String role = roles[i];

            player.assignRole(role);
            assignments.put(player.getPlayerId(),role);
        }

        return Map.copyOf(assignments);
    }
}

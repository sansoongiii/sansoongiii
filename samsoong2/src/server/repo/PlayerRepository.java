package server.repo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import server.db.DBManager;

public class PlayerRepository {

    // 게임 시작 시 접속한 플레이어들의 방 코드, 카테고리 및 초기 문제 수(0, 0) 삽입
    public boolean savePlayers(List<String> playerIds, String roomCode, String category) {
        Connection conn = DBManager.getConnection();
        if (conn == null) return false;

        PreparedStatement pstmt = null;
        String sql = "INSERT INTO player (PlayerID, RoomCode, Category, RoundCount, CorrectCount) VALUES (?, ?, ?, 0, 0)";

        try {
            pstmt = conn.prepareStatement(sql);
            for (String playerId : playerIds) {
                pstmt.setString(1, playerId);
                pstmt.setString(2, roomCode);
                pstmt.setString(3, category != null ? category : "");
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            return results.length > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBManager.close(conn, pstmt);
        }
    }

    // 라운드 종료 시 푼 문제 수와 맞춘 문제 수 업데이트
    public boolean updateRoundResult(List<String> playerIds, boolean isCorrect) {
        Connection conn = DBManager.getConnection();
        if (conn == null) return false;

        PreparedStatement pstmt = null;
        String sql = "UPDATE player SET RoundCount = RoundCount + 1, CorrectCount = CorrectCount + ? WHERE PlayerID = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            for (String playerId : playerIds) {
                pstmt.setInt(1, isCorrect ? 1 : 0);
                pstmt.setString(2, playerId);
                pstmt.addBatch();
            }

            int[] results = pstmt.executeBatch();
            return results.length > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBManager.close(conn, pstmt);
        }
    }
}
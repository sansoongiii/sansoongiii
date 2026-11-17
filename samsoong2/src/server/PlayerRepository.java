package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PlayerRepository {

    // 접속한 사람들 아이디 플레이어 테이블에 삽입
    public boolean savePlayers(List<String> playerIds) {
        Connection conn = DBManager.getConnection();
        if (conn == null) return false;

        PreparedStatement pstmt = null;
        String joinedPlayers = "[\"" + String.join("\",\"", playerIds) + "\"]"; // "player1,player2,player3" 형태
        String sql = "INSERT INTO player (Player) VALUES (?)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, joinedPlayers);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBManager.close(conn, pstmt);
        }
    }

    // 랜덤 배정된 역할을 플레이어 테이블에 업데이트
    public boolean updateRoles(List<String> playerIds, String noSee, String noHear, String noSpeak) {
        Connection conn = DBManager.getConnection();
        if (conn == null) return false;

        PreparedStatement pstmt = null;
        String joinedPlayers = "[\"" + String.join("\",\"", playerIds) + "\"]";
        String sql = "UPDATE player SET NoSee = ?, NoHear = ?, NoSpeak = ? WHERE Player = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, noSee);
            pstmt.setString(2, noHear);
            pstmt.setString(3, noSpeak);
            pstmt.setString(4, joinedPlayers);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            DBManager.close(conn, pstmt);
        }
    }
}
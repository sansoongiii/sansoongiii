package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserRepository {

    public boolean saveUser(String id, String pw) {
        Connection conn = DBManager.getConnection();
        if (conn == null) return false;

        PreparedStatement pstmt = null;
        String sql = "INSERT INTO user (ID, PW) VALUES (?, ?)";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, pw);

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
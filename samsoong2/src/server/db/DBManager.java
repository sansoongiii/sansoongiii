package server.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBManager {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/game";
    //private static final String USER = "Tester";
    private static final String USER = "user2";
    //private static final String PASSWD = "3soong";
    private static final String PASSWD = "1578";

    // DB 연결
    public static Connection getConnection() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(URL, USER, PASSWD);
        } catch (SQLException e) {
            System.out.println("DB 연결 실패!");
            e.printStackTrace();
        }
        return conn;
    }

    // 사용한 자원 닫기 - SELECT
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            if (rs != null) rs.close();
            if (pstmt != null) pstmt.close();
            if (conn != null) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 사용한 자원 닫기 - INSERT, UPDATE, DELETE
    public static void close(Connection conn, PreparedStatement pstmt) {
        close(conn, pstmt, null);
    }
}
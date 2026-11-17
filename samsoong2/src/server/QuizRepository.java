package server;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class QuizRepository {

    public List<QuizDto> findByCategory(String category) {
        List<QuizDto> list = new ArrayList<>();
        Connection conn = DBManager.getConnection();
        if (conn == null) return list;

        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT KeywordNo, Category, Keyword FROM quiz WHERE Category = ?";

        try {
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, category);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int no = rs.getInt("KeywordNo");
                String cat = rs.getString("Category");
                String key = rs.getString("Keyword");
                list.add(new QuizDto(no, cat, key));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, pstmt, rs);
        }
        return list;
    }
    
    public List<String> findCategories() {
    	List<String> categories = new ArrayList<>();
    	Connection conn = DBManager.getConnection();
        if (conn == null) return categories;
		
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String sql = "SELECT DISTINCT Category FROM quiz WHERE Category IS NOT NULL"; // 중복 없이 반환
        
        try {
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                categories.add(rs.getString("Category"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBManager.close(conn, pstmt, rs);
        }
        return categories;
    }
}
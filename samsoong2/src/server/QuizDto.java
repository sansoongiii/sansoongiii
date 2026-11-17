package server;

public class QuizDto {
    private int keywordNo;
    private String category;
    private String keyword;

    public QuizDto(int keywordNo, String category, String keyword) {
        this.keywordNo = keywordNo;
        this.category = category;
        this.keyword = keyword;
    }

    public int getKeywordNo() { return keywordNo; }
    public String getCategory() { return category; }
    public String getKeyword() { return keyword; }
}
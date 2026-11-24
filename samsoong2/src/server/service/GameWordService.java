package server.service;

import java.util.List;

import server.db.QuizDto;
import server.repo.QuizRepository;

public class GameWordService {
    private final QuizRepository quizRepository;

    public GameWordService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public List<String> getWords(String category) {
        List<QuizDto> quizzes =
            quizRepository.findByCategory(category);

        if (quizzes.isEmpty()) {
            throw new IllegalArgumentException(
                "존재하지 않거나 단어가 없는 카테고리입니다: "
                    + category
            );
        }

        return quizzes.stream()
                .map(QuizDto::getKeyword)
                .distinct()
                .toList();
    }

    public List<String> getCategories() {
        return quizRepository.findCategories();
    }
}

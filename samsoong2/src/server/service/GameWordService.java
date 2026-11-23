package server.service;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import server.db.QuizDto;
import server.repo.QuizRepository;

public class GameWordService {
    private final QuizRepository quizRepository;

    public GameWordService(QuizRepository quizRepository) {
        this.quizRepository = quizRepository;
    }

    public String getRandomWord(String category) {
        List<QuizDto> quizzes =
            quizRepository.findByCategory(category);

        if (quizzes.isEmpty()) {
            throw new IllegalArgumentException(
                "존재하지 않거나 단어가 없는 카테고리입니다: "
                    + category
            );
        }

        int index = ThreadLocalRandom.current().nextInt(quizzes.size());

        return quizzes.get(index).getKeyword();
    }

    public List<String> getCategories() {
        return quizRepository.findCategories();
    }
}

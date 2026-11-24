package server;

import java.io.IOException;

import server.network.GameServer;
import server.repo.QuizRepository;
import server.service.GameTimerService;
import server.service.GameWordService;
import server.service.RoleAssignmentService;
import server.service.SubmissionValidator;

public class ServerMain {

    public static void main(String[] args) {

        QuizRepository quizRepository = new QuizRepository();

        GameWordService gameWordService =
                new GameWordService(quizRepository);

        RoleAssignmentService roleAssignmentService =
                new RoleAssignmentService();

        SubmissionValidator submissionValidator =
                new SubmissionValidator();

        GameTimerService gameTimerService =
                new GameTimerService();

        // GameServer가 GameMessageSender 역할도 수행
        GameServer server = new GameServer(
                roleAssignmentService,
                gameWordService,
                submissionValidator,
                gameTimerService
        );

        try {
            server.start();
        } catch (IOException e) {
            System.out.println("서버 실행 오류: " + e.getMessage());
        }
    }
}
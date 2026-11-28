package client.controller;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import client.network.GameClient;
import client.ui.GameFrame;
import client.ui.RoomEntryPanel;
import client.ui.WaitingRoomPanel;
import common.Message;

public class ViewController {

    private final GameClient client;
    private final ConnectionController connectionController;
    private final GameController gameController;

    private GameFrame gameFrame;
    
    public ViewController(GameClient client, ConnectionController connectionController, GameController gameController) {
        this.client = client;
        this.connectionController = connectionController;
        this.gameController = gameController;
    }

    public boolean initGui() {
        try {
            SwingUtilities.invokeAndWait(() -> {
                GameFrame frame = new GameFrame();

                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                frame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        frame.stopCountdown();
                        client.close();
                        frame.dispose();
                    }
                });

                this.gameFrame = frame;

                connectProgressListeners();
                connectGameButtons();
                connectLobbyButtons();
                connectPostGameButtons();

                frame.showRoomEntry();
                frame.setVisible(true);
            });
            return true;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            client.close();
            return false;

        } catch (InvocationTargetException e) {
            client.close();
            throw new RuntimeException("게임 화면 생성에 실패했습니다.", e.getCause());
        }
    }

    public void closeGui() {
        SwingUtilities.invokeLater(() -> {
            if (gameFrame != null) {
                gameFrame.stopCountdown();
                gameFrame.dispose();
            }
        });
    }

    public void handleMessage(Message message) {
        SwingUtilities.invokeLater(() -> {
            switch (message.getType()) {
                case ROOM_UPDATED:
                    handleRoomUpdated(message);
                    break;

                case CATEGORY_LIST:
                    // 방장 화면에만 카테고리 개수만큼 버튼 자동 생성
                    gameFrame.getWaitingRoomPanel().showCategoryButtons(
                        message.getCategories()
                    );
                    break;

                case PHASE_STARTED:
                    handlePhaseStarted(message);
                    break;

                case ROUND_RESULT:
                    handleRoundResult(message);
                    break;

                case CONTINUE_STATUS:
                    gameFrame.getResultPanel().setContinueStatus(
                        message.getAgreedCount(),
                        message.getPlayerCount()
                    );
                    break;

                case POST_GAME_OPTIONS:
                    handlePostGameOptions(message);
                    break;

                case WAITING_FOR_HOST:
                    handleWaitingForHost(message);
                    break;

                case ERROR:
                    handleError(message);
                    break;

                case ROOM_LEFT:
                case GAME_ABORTED:
                    gameFrame.stopCountdown();
                    JOptionPane.showMessageDialog(gameFrame, message.getNotice());
                    gameFrame.getRoomEntryPanel().setButtonsEnabled(true);
                    gameFrame.showRoomEntry();
                    break;

                case GAME_FINISHED:
                    gameFrame.stopCountdown();
                    JOptionPane.showMessageDialog(gameFrame, message.getNotice());
                    gameFrame.dispose();
                    client.close();
                    break;

                default:
                    break;
            }
        });
    }

    private void handleRoomUpdated(Message message) {
        gameFrame.getWaitingRoomPanel().updateRoomStatus(
            message.getRoomCode(),
            message.getPlayerCount()
        );
        gameFrame.showWaitingRoom();

        // 3명이 모두 모이면 서버에 카테고리 목록 요청
        if (message.getPlayerCount() == 3) {
            try {
                connectionController.requestCategories();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void handlePhaseStarted(Message message) {
        String role = message.getRole();
        String phase = message.getPhase();
        int round = message.getRoundNumber();

        switch (phase) {
            case "TEXT_WRITING":
                gameFrame.setPhase("설명 작성 단계");
                break;
            case "DRAWING":
                gameFrame.setPhase("그림 그리기 단계");
                break;
            case "ANSWERING":
                gameFrame.setPhase("정답 맞히기 단계");
                break;
            default:
                gameFrame.setPhase("게임 진행 중");
                break;
        }

        gameFrame.startCountdown(message.getDeadlineEpochMillis(), () -> setRoleInputEnabled(role, phase, false));

        if ("TEXT_WRITER".equals(role)) {
            gameFrame.showTextWriter(
                round,
                "TEXT_WRITING".equals(phase) ? message.getAnswer() : ""
            );
        } else if ("DRAWER".equals(role)) {
            gameFrame.showDrawing(
                round,
                "DRAWING".equals(phase) ? message.getTextDescription() : ""
            );
        } else if ("ANSWERER".equals(role)) {
            gameFrame.showAnswerer(
                round,
                "ANSWERING".equals(phase) ? message.getDrawingData() : null
            );
        }

        setRoleInputEnabled(role, phase, true);
    }
    
    private void handleRoundResult(Message message) {
        gameFrame.stopCountdown();

        gameFrame.getTextWriterPanel().setInputEnabled(false);
        gameFrame.getDrawingPanel().setCanDraw(false);
        gameFrame.getDrawingPanel().getSubmitButton().setEnabled(false);
        gameFrame.getAnswererPanel().setInputEnabled(false);

        gameFrame.setPhase("라운드 결과");

        gameFrame.showResult(
            message.getRoundNumber(),
            message.getAnswer(),
            message.getTextDescription(),
            message.getDrawingData(),
            message.getSubmittedAnswer(),
            message.isCorrect()
        );
    }

    private void handlePostGameOptions(Message message) {
        gameFrame.stopCountdown(); 

        gameFrame.showPostGame(
            true,
            message.getNotice()
        );
    }

    private void handleWaitingForHost(Message message) {
        gameFrame.stopCountdown();

        gameFrame.showPostGame(
            false,
            message.getNotice()
        );
    }

    private void handleError(Message message) {
        JOptionPane.showMessageDialog(
            gameFrame,
            message.getNotice(),
            "서버 오류: " + message.getErrorCode(),
            JOptionPane.ERROR_MESSAGE
        );
        String errorCode = message.getErrorCode();
        if ("TEXT_VALIDATION_FAILED".equals(errorCode)) {
            // 제시어 검증 실패: 다시 입력할 수 있도록 활성화
            gameFrame.getTextWriterPanel().setInputEnabled(true);
        } else if ("ROOM_NOT_FOUND".equals(errorCode)
                || "ROOM_FULL".equals(errorCode)
                || "ALREADY_IN_ROOM".equals(errorCode)
                || "INVALID_PHASE".equals(errorCode)) {
            // 방 입장/생성 오류: 창을 닫지 않고 버튼만 다시 활성화
            gameFrame.getRoomEntryPanel().setButtonsEnabled(true);
        } else {
            // 그 외 오류: 게임 창 종료
            gameFrame.stopCountdown();
            gameFrame.dispose();
            client.close();
        }
    }

    private void setRoleInputEnabled(String role, String phase, boolean allowActive) {
        boolean isTextWriter = allowActive && "TEXT_WRITER".equals(role) && "TEXT_WRITING".equals(phase);
        boolean isDrawer = allowActive && "DRAWER".equals(role) && "DRAWING".equals(phase);
        boolean isAnswerer = allowActive && "ANSWERER".equals(role) && "ANSWERING".equals(phase);

        gameFrame.getTextWriterPanel().setInputEnabled(isTextWriter);
        gameFrame.getDrawingPanel().setCanDraw(isDrawer);
        gameFrame.getDrawingPanel().getSubmitButton().setEnabled(isDrawer);
        gameFrame.getAnswererPanel().setInputEnabled(isAnswerer);
    }

    private void connectProgressListeners() {
        //자동 저장 연결
        gameFrame.getTextWriterPanel().setProgressListener(description -> {
           
        	try {
            	 gameController.saveTextProgress(description);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        gameFrame.getAnswererPanel().setProgressListener(answer -> {

            try {
            	 gameController.saveAnswerProgress(answer);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        gameFrame.getDrawingPanel().setProgressListener(drawingData -> {
        	
            try {
            	 gameController.saveDrawingProgress(drawingData);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
    }

    private void connectGameButtons() {
        // 설명 제출
        gameFrame.getTextWriterPanel().getSubmitButton().addActionListener(e -> {

            String description = gameFrame.getTextWriterPanel().getDescription();

            if (description.isBlank()) {
                JOptionPane.showMessageDialog(gameFrame, "설명을 입력하세요.");
                return;
            }

            try {
            	gameController.submitText(description);
                gameFrame.getTextWriterPanel().setInputEnabled(false);
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(gameFrame, "설명 전송에 실패했습니다.");
                ex.printStackTrace();
            }
        });

        // 그림 제출
        gameFrame.getDrawingPanel().getSubmitButton().addActionListener(e -> {

            try {
                byte[] drawingData = gameFrame.getDrawingPanel().getDrawingData();

                gameController.submitDrawing(drawingData);

                gameFrame.getDrawingPanel().getSubmitButton().setEnabled(false);
                gameFrame.getDrawingPanel().setCanDraw(false);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(gameFrame, "그림 전송에 실패했습니다.");
                ex.printStackTrace();
            }
        });

        // 정답 제출
        gameFrame.getAnswererPanel().getSubmitButton().addActionListener(e -> {

            String answer = gameFrame.getAnswererPanel().getAnswer();

            if (answer.isBlank()) {
                JOptionPane.showMessageDialog(gameFrame, "정답을 입력하세요.");
                return;
            }

            try {
            	gameController.submitAnswer(answer);
                gameFrame.getAnswererPanel().setInputEnabled(false);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(gameFrame, "정답 전송에 실패했습니다.");
                ex.printStackTrace();
            }
        });

        // 다음 라운드 진행
        gameFrame.getResultPanel().getNextButton().addActionListener(e -> {

            System.out.println(
                    "[버튼 클릭] 라운드=" + gameController.getCurrentRound()
                            + " / 연결=" + client.isConnected()
            );

            try {
                System.out.println("[다음 라운드] 전송 시작");

                gameController.continueToNextRound();

                System.out.println("[다음 라운드] 전송 완료");

                gameFrame.getResultPanel().getNextButton()
                        .setText("확인 완료 — 다른 참가자 대기");

                gameFrame.getResultPanel().getNextButton()
                        .setEnabled(false);

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                        gameFrame,
                        "다음 라운드 요청에 실패했습니다."
                );
                ex.printStackTrace();
            }
        });
    }

    private void connectLobbyButtons() {
        RoomEntryPanel entryPanel = gameFrame.getRoomEntryPanel();
        WaitingRoomPanel waitingPanel = gameFrame.getWaitingRoomPanel();

        // 방 생성
        entryPanel.getCreateRoomButton().addActionListener(e -> {
            try {
                waitingPanel.setHost(true);
                entryPanel.setButtonsEnabled(false);
                connectionController.createRoom();
            } catch (IOException ex) {
                entryPanel.setButtonsEnabled(true);
                JOptionPane.showMessageDialog(gameFrame, "방 생성 요청에 실패했습니다.");
                ex.printStackTrace();
            }
        });

    

        // 방 입장
        entryPanel.getJoinRoomButton().addActionListener(e -> {
            String roomCode = JOptionPane.showInputDialog(gameFrame, "입장할 방 코드를 입력하세요:");
            if (roomCode == null || roomCode.isBlank()) {
                return;
            }
            try {
                waitingPanel.setHost(false);
                entryPanel.setButtonsEnabled(false);
                connectionController.joinRoom(roomCode.trim());
            } catch (IOException ex) {
                entryPanel.setButtonsEnabled(true);
                JOptionPane.showMessageDialog(gameFrame, "방 입장 요청에 실패했습니다.");
                ex.printStackTrace();
            }
        });

        // 카테고리 선택
        waitingPanel.setCategorySelectListener(selectedCategory -> {
            try {
            	connectionController.selectCategory(selectedCategory);
            	connectionController.startGame();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(gameFrame, "카테고리 선택 전송에 실패했습니다.");
                ex.printStackTrace();
            }
        });

        // 방 나가기
        waitingPanel.getLeaveRoomButton().addActionListener(e -> {
            try {
            	connectionController.leaveRoom();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(gameFrame, "방 나가기 요청에 실패했습니다.");
                ex.printStackTrace();
            }
        });
    }
    
    private void connectPostGameButtons() {
        // 재시작 버튼
        gameFrame.getPostGamePanel().setRestartListener(() -> {

            try {
            	connectionController.restartGame();

                // 카테고리 선택을 기다리는 화면으로 변경
                gameFrame.showPostGame(
                    false,
                    "새로운 게임을 준비하고 있습니다. 잠시 기다려주세요."
                );

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    gameFrame,
                    "재시작 요청에 실패했습니다."
                );
                ex.printStackTrace();
            }
        });

        // 게임 종료 버튼
        gameFrame.getPostGamePanel().setEndListener(() -> {
            int choice = JOptionPane.showConfirmDialog(
                gameFrame,
                "정말 게임을 종료하시겠습니까?",
                "게임 종료",
                JOptionPane.YES_NO_OPTION
            );

            if (choice != JOptionPane.YES_OPTION) {
                return;
            }

            try {
            	connectionController.endGame();
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(
                    gameFrame,
                    "게임 종료 요청에 실패했습니다."
                );
                ex.printStackTrace();
            }
        });
    }
}
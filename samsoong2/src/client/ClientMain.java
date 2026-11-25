package client;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import client.network.GameClient;
import client.ui.*;
import common.Message;
import common.MessageType;

public class ClientMain {

    public static void main(String[] args) {

    	GameClient client = new GameClient();
    	    	
    	try {
            client.connect("127.0.0.1");
            client.startReceiving();
        
            BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
            System.out.println("1: 방 생성 / 2: 방 입장");
            String choice = keyboard.readLine();

            Message request = new Message();
            
            boolean isHost = false;
            
            if ("1".equals(choice)) {
				request.setType(MessageType.CREATE_ROOM);
				isHost = true;
            } else if("2".equals(choice)) {
				System.out.println("방 코드 입력: ");
				String roomCode = keyboard.readLine();
				
				if(roomCode == null || roomCode.isBlank()) {
					System.out.println("방 코드를 입력해야 합니다.");
					client.close();
					return;
				}
				
				request.setType(MessageType.JOIN_ROOM);
				request.setRoomCode(roomCode.trim());
			} else {
				System.out.println("1 또는 2를 선택해야 합니다.");
				client.close();
				return;
			}
        
            client.send(request);
            
            if(isHost) {
                List<String> categories = client.waitForCategories();
                if (categories == null) {
                    System.out.println("서버 연결이 종료되었습니다.");
                    client.close();
                    return;
                }
                
                System.out.println("\n카테고리 목록:");

                for (int i = 0; i < categories.size(); i++) {
                    System.out.println((i + 1) + ". " + categories.get(i));
                }

                System.out.println("카테고리 번호를 선택하세요: ");
                String input = keyboard.readLine();

                int selectedIndex = Integer.parseInt(input) - 1;

                if (selectedIndex < 0 || selectedIndex >= categories.size()) {
                    System.out.println("잘못된 카테고리 번호입니다.");
                    client.close();
                    return;
                }

                String selectedCategory = categories.get(selectedIndex);

                Message categoryRequest = new Message();
                categoryRequest.setType(MessageType.SELECT_CATEGORY);
                categoryRequest.setCategory(selectedCategory);

                client.send(categoryRequest);

                System.out.println("선택한 카테고리: " + selectedCategory);
                
                System.out.println("게임을 시작하려면 1, 시작하지 않으려면 0을 입력하세요.");
                String startChoice = keyboard.readLine();

                if ("1".equals(startChoice)) {
                    Message startRequest = new Message();
                    startRequest.setType(MessageType.START_GAME);
                    client.send(startRequest);

                    System.out.println("게임 시작 요청을 전송했습니다.");
                } else if ("0".equals(startChoice)) {
                    System.out.println("게임 시작을 취소했습니다.");
                    client.close();
                    return;
                } else {
                    System.out.println("잘못된 입력입니다.");
                    client.close();
                    return;
                }
            }
            // GUI 기반 게임 진행 시작
            startGameGui(client);
        } catch (IOException e) {
            System.out.println(
                    "연결 또는 입출력 오류: " + e.getMessage()
            );

            client.close();
    	} catch (InterruptedException e) {
    		System.out.println("카테고리 대기가 중단되었습니다.");
    		Thread.currentThread().interrupt();
    		client.close();
    		
    	} catch (NumberFormatException e) {
            System.out.println("카테고리 번호는 숫자로 입력해야 합니다.");
            client.close();
        }
    }
    private static void startGameGui(GameClient client) {
    	GameFrame[] frameHolder = new GameFrame[1];
    	int[] currentRound = {0};
    	
        try {
            SwingUtilities.invokeAndWait(() -> {
                GameFrame frame = new GameFrame();
                
                frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

                frame.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        frame.stopCountdown();
                        client.close();
                        frame.dispose();
                    }
                });
                
                //자동 저장 연결
                frame.getTextWriterPanel().setProgressListener(description -> {
                    Message request = new Message();
                    request.setType(MessageType.SAVE_TEXT_PROGRESS);
                    request.setRoundNumber(currentRound[0]);
                    request.setTextDescription(description);

                    try {
                        client.send(request);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                frame.getAnswererPanel().setProgressListener(answer -> {
                    Message request = new Message();
                    request.setType(MessageType.SAVE_ANSWER_PROGRESS);
                    request.setRoundNumber(currentRound[0]);
                    request.setSubmittedAnswer(answer);

                    try {
                        client.send(request);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                frame.getDrawingPanel().setProgressListener(drawingData -> {
                    Message request = new Message();
                    request.setType(MessageType.SAVE_DRAWING_PROGRESS);
                    request.setRoundNumber(currentRound[0]);
                    request.setDrawingData(drawingData);

                    try {
                        client.send(request);
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                });
                
                connectGameButtons(client, frame, currentRound);
                frame.setVisible(true);

                frameHolder[0] = frame;
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            client.close();
            return;

        } catch (java.lang.reflect.InvocationTargetException e) {
            client.close();
            throw new RuntimeException("게임 화면 생성에 실패했습니다.", e.getCause());
        }

        GameFrame gameFrame = frameHolder[0];

        Thread gameThread = new Thread(() -> {

            try {
                while (true) {

                    Message message = client.waitForPhaseStarted();
                    if (message == null) {
                    	SwingUtilities.invokeLater(() -> {
                            gameFrame.stopCountdown();
                            gameFrame.dispose();
                        });
                    	break;
                    }
                    SwingUtilities.invokeLater(() -> {

                        switch (message.getType()) {

                            case PHASE_STARTED:
                            	String role = message.getRole();
                            	String phase = message.getPhase();
                            	int round = message.getRoundNumber();
                            	currentRound[0] = round;

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
                                gameFrame.startCountdown(message.getDeadlineEpochMillis(),() -> {
                                	        if ("TEXT_WRITER".equals(role)) {
                                	            gameFrame.getTextWriterPanel().setInputEnabled(false);
                                	        } else if ("DRAWER".equals(role)) {
                                	            gameFrame.getDrawingPanel().getSubmitButton().setEnabled(false);
                                	            gameFrame.getDrawingPanel().setCanDraw(false);
                                	        } else if ("ANSWERER".equals(role)) {
                                	            gameFrame.getAnswererPanel().setInputEnabled(false);
                                	        }
                                	}
                                );
                                
                                if ("TEXT_WRITER".equals(role)) {
                                    gameFrame.showTextWriter(
                                        round,
                                        message.getAnswer()
                                    );

                                } else if ("DRAWER".equals(role)) {
                                    gameFrame.showDrawing(
                                        round,
                                        message.getTextDescription()
                                    );

                                } else if ("ANSWERER".equals(role)) {
                                    gameFrame.showAnswerer(
                                        round,
                                        message.getDrawingData()
                                    );
                                }
                            	setRoleInputEnabled(gameFrame, role, phase);
                                break;

                            case ROUND_RESULT:
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
                                break;
                            case CONTINUE_STATUS:
                                gameFrame.getResultPanel().setContinueStatus(
                                    message.getAgreedCount(),
                                    message.getPlayerCount()
                                );
                                break;
                            case ERROR:
                                JOptionPane.showMessageDialog(
                                    gameFrame,
                                    message.getNotice(),
                                    "서버 오류: " + message.getErrorCode(),
                                    JOptionPane.ERROR_MESSAGE
                                );
                                if ("TEXT_VALIDATION_FAILED".equals(message.getErrorCode())) {
                                    // 제시어 검증 실패: 다시 입력할 수 있도록 활성화
                                    gameFrame.getTextWriterPanel().setInputEnabled(true);
                                } else {
                                    // 그 외 서버 오류: 게임 창 종료
                                    gameFrame.stopCountdown();
                                    gameFrame.dispose();
                                    client.close();
                                }
                                break;
                            case GAME_FINISHED:
                            case ROOM_LEFT:
                            case GAME_ABORTED:
                            	gameFrame.stopCountdown();
                                JOptionPane.showMessageDialog(
                                    gameFrame,
                                    message.getNotice()
                                );

                                gameFrame.dispose();
                                client.close();
                                break;

                            default:
                                break;
                        }
                    });

                    if (message.getType() == MessageType.GAME_FINISHED
                            || message.getType() == MessageType.ROOM_LEFT
                            || message.getType() == MessageType.GAME_ABORTED) {
                        break;
                    }
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

        });

        gameThread.start();
    }
    
    private static void setRoleInputEnabled(GameFrame gameFrame, String role, String phase) {

        boolean isTextWriter = "TEXT_WRITER".equals(role) && "TEXT_WRITING".equals(phase);
        boolean isDrawer = "DRAWER".equals(role) && "DRAWING".equals(phase);
        boolean isAnswerer = "ANSWERER".equals(role) && "ANSWERING".equals(phase);

        gameFrame.getTextWriterPanel().setInputEnabled(isTextWriter);
        gameFrame.getDrawingPanel().setCanDraw(isDrawer);
        gameFrame.getDrawingPanel().getSubmitButton().setEnabled(isDrawer);
        gameFrame.getAnswererPanel().setInputEnabled(isAnswerer);
    }
    
    private static void connectGameButtons(GameClient client, GameFrame gameFrame, int[] currentRound) {

        // 설명 제출
        gameFrame.getTextWriterPanel().getSubmitButton().addActionListener(e -> {

            String description = gameFrame.getTextWriterPanel().getDescription();

            if (description.isBlank()) {
                JOptionPane.showMessageDialog(gameFrame, "설명을 입력하세요.");
                return;
            }

            Message request = new Message();
            request.setType(MessageType.SUBMIT_TEXT);
            request.setRoundNumber(currentRound[0]);
            request.setTextDescription(description);

            try {
                client.send(request);
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

                Message request = new Message();
                request.setType(MessageType.SUBMIT_DRAWING);
                request.setRoundNumber(currentRound[0]);
                request.setDrawingData(drawingData);

                client.send(request);

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

            Message request = new Message();
            request.setType(MessageType.SUBMIT_ANSWER);
            request.setRoundNumber(currentRound[0]);
            request.setSubmittedAnswer(answer);

            try {
                client.send(request);
                gameFrame.getAnswererPanel().setInputEnabled(false);
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(gameFrame, "정답 전송에 실패했습니다.");
                ex.printStackTrace();
            }
        });


     // 다음 라운드 진행
        gameFrame.getResultPanel().getNextButton().addActionListener(e -> {

			
            System.out.println(
                    "[버튼 클릭] 라운드=" + currentRound[0]
                            + " / 연결=" + client.isConnected()
            );
			
            Message request = new Message();
            request.setType(MessageType.CONTINUE_RESPONSE);
            request.setRoundNumber(currentRound[0]);
            request.setAgree(true);

            try {
                System.out.println("[다음 라운드] 전송 시작");

                client.send(request);

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
}

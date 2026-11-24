package client.network;

import java.io.BufferedReader;
import java.io.IOException;

import common.Message;
import common.PlayerRole;
import common.Protocol;

public class ClientReceiver implements Runnable {

    private final BufferedReader reader;
    private final GameClient client;

    public ClientReceiver(BufferedReader reader, GameClient client) {
        this.reader = reader;
        this.client = client;
    }

    @Override
    public void run() {
       try {
    	   String line; 
    	   while((line = reader.readLine()) != null) {
    		   try {
    			   Message message = Protocol.parse(line);
                   printMessage(message);

    		   } catch (IllegalArgumentException e) {
    			   System.out.println("메시지 형식 오류: " + e.getMessage());
    		   }
    	   }
    	   if (client.isConnected()) {
			System.out.println("서버 연결이 종료되었습니다.");
		}
       } catch (IOException e) {
    	   if (client.isConnected()) {
			System.out.println("수신 오류: " + e.getMessage());
		}
       } finally {
    	   client.close();
       }
    }
    
    private void printMessage(Message message) {
        System.out.println(
                "[서버 메시지] " + message.getType()
        );

        switch (message.getType()) {
            case CONNECTED:
                System.out.println(
                        "[내 ID] " + message.getPlayerId()
                );
                break;

            case CATEGORY_LIST:
            	client.receiveCategories(message.getCategories());
                System.out.println(
                        "[카테고리] " + message.getCategories()
                );
                break;

            case ROOM_UPDATED:
                System.out.printf(
                        "[방] %s / 인원 %d / 카테고리 %s%n",
                        message.getRoomCode(),
                        message.getPlayerCount(),
                        message.getCategory()
                );
                break;

            case PHASE_STARTED:
            	client.setCurrentRole(message.getRole());
            	client.receivePhaseStarted(message);
            	
                System.out.printf(
                        "[라운드 %d] 단계 %s / 역할 %s%n",
                        message.getRoundNumber(),
                        message.getPhase(),
                        message.getRole()
                );

                if (message.getAnswer() != null && !message.getAnswer().isEmpty()) {
                    System.out.println("[제시어] " + message.getAnswer());
                }
                
                if (PlayerRole.DRAWER.name().equals(message.getRole()) && message.getTextDescription() != null && !message.getTextDescription().isEmpty()) {
                    System.out.println("[Writer의 설명] " + message.getTextDescription());
                }
                break;

            case ROUND_RESULT:
                System.out.printf(
                        "[결과] 제시어 %s / 답안 %s / 정답 여부 %s%n",
                        message.getAnswer(),
                        message.getSubmittedAnswer(),
                        message.isCorrect()
                );
                break;

            case CONTINUE_STATUS:
                System.out.printf(
                        "[계속 진행] %d/%d%n",
                        message.getAgreedCount(),
                        message.getPlayerCount()
                );
                break;

            case ERROR:
                System.out.println(
                        "[오류 " + message.getErrorCode() + "] "
                                + message.getNotice()
                );
                break;

            case GAME_FINISHED:
            case ROOM_LEFT:
            case GAME_ABORTED:
            case TEST_MESSAGE:
                System.out.println(
                        "[안내] " + message.getNotice()
                );
                break;

            default:
                System.out.println(
                        "서버 알림으로 처리하지 않는 종류입니다."
                );
                break;
                
           }
     }
}

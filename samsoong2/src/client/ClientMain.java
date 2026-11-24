package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import common.GamePhase;
import client.network.GameClient;
import common.Message;
import common.MessageType;
import common.PlayerRole;

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

                System.out.println("\n카테고리 목록:");

                for (int i = 0; i < categories.size(); i++) {
                    System.out.println((i + 1) + ". " + categories.get(i));
                }

                System.out.println("카테고리 번호를 선택하세요: ");
                String input = keyboard.readLine();

                int selectedIndex = Integer.parseInt(input) - 1;

                if (selectedIndex < 0 || selectedIndex >= categories.size()) {
                    System.out.println("잘못된 카테고리 번호입니다.");
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
                } else {
                    System.out.println("잘못된 입력입니다.");
                }
            }
            
            while (client.isConnected()) {

                Message phaseMessage = client.waitForPhaseStarted();

                String role = phaseMessage.getRole();

                if (GamePhase.TEXT_WRITING.name().equals(phaseMessage.getPhase())
                        && PlayerRole.TEXT_WRITER.name().equals(role)) {

                    System.out.println("제시어를 보고 설명을 입력하세요.");
                    String description = keyboard.readLine();

                    Message textRequest = new Message();
                    textRequest.setType(MessageType.SUBMIT_TEXT);
                    textRequest.setTextDescription(description);

                    client.send(textRequest);

                    System.out.println("설명을 제출했습니다.");
                }
            }

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
}
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import common.Message;
import common.MessageType;

public class ClientMain {

    public static void main(String[] args) {

    	GameClient client = new GameClient();
    	
    	BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
    	
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
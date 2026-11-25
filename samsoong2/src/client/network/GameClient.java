package client.network;

import common.Message;
import common.MessageType;
import common.Protocol;

import client.controller.GameController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameClient {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread receiveThread;
    
    private volatile boolean connected;
    private volatile String currentRole;
    
    private final GameController controller;
    private final AtomicBoolean closed = new AtomicBoolean(false);
    
    private final BlockingQueue<List<String>> categoryQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> phaseQueue = new LinkedBlockingQueue<>();
    
    public GameClient() {
        this(new GameController());
    }

    public GameClient(GameController controller) {
        this.controller = Objects.requireNonNull(controller);
    }
    
    public void connect(String serverIp) throws IOException {
    	
    	if (closed.get()) {
            throw new IOException(
                    "종료한 클라이언트입니다. 새 객체로 연결하세요."
            );
        }
    	
        if (socket != null) {
            throw new IOException("이미 연결을 시도한 클라이언트입니다.");
        }

        socket = new Socket();

    	try {
            socket.connect(new InetSocketAddress(serverIp,Protocol.PORT), 5000);

            reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

            connected = true;
            System.out.println("서버 연결 완료!");

        } catch (IOException e) {
            socket.close();
            throw e;
        }    	 	
    }

    public void startReceiving() {
        if (!connected) {
			throw new IllegalStateException("먼저 서버에 연결해야 합니다.");
		}
        if (receiveThread != null) {
        	return;
		}
        ClientReceiver receiver = new ClientReceiver(reader, this, controller);       
        receiveThread = new Thread(receiver, "client-receiver");
        receiveThread.start();
    }

    public synchronized void send(Message message) throws IOException {
        // 서버에 메시지 전송
    	if (!connected) {
			throw new IOException("서버에 연결되어 있지 않습니다.");
		}
    	writer.println(Protocol.build(message));
    	if (writer.checkError()) {
			throw new IOException("메시지 전송에 실패했습니다.");
		}
    }

    public boolean isConnected() {
        return connected;
    }

    public synchronized void close() {
    	if (!closed.compareAndSet(false, true)) {
   	        return;
        }

    	connected = false;
                
        try {
			if(socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("연결 종료 오류: " + e.getMessage());
		}
    }
    
    public void disconnect() {
        try {
            if (connected) {
                Message request = new Message();
                request.setType(MessageType.DISCONNECT);
                send(request);
            }
        } catch (IOException e) {
            System.out.println(
                    "종료 요청 전송 실패: " + e.getMessage()
            );
        } finally {
            close();
        }
    }
    
    public void receiveCategories(List<String> categories) {
        categoryQueue.offer(List.copyOf(categories));
    }
    
    public List<String> waitForCategories() throws InterruptedException, IOException {
    	while (connected) {
            List<String> categories = categoryQueue.poll(200, TimeUnit.MILLISECONDS);

            if (categories != null) {
                return categories;
            }
        }
        throw new IOException("서버 연결이 종료되었습니다."); 
    }

    public void setCurrentRole(String role) {
      currentRole = role;
    }

    public String getCurrentRole() {
        return currentRole;
    }
    
    public void receivePhaseStarted(Message message) {
        phaseQueue.offer(message);
    }
    
    public void receiveRoundResult(Message message) {
        phaseQueue.offer(message);
    }
    
    public void receiveTerminalMessage(Message message) {
        phaseQueue.offer(message);
    }
    
    public void receiveContinueStatus(Message message) {
        phaseQueue.offer(message);
    }

    public Message waitForPhaseStarted() throws InterruptedException {
    	 while (connected || !phaseQueue.isEmpty()) {
    	        Message message = phaseQueue.poll(200, TimeUnit.MILLISECONDS);

    	        if (message != null) {
    	            return message;
    	        }
    	    }
    	 
    	 	return null;
    }
    
 // 제출 버튼을 눌렀을 때 호출
    public void submitDrawing(
            int roundNumber,
            byte[] drawingData) throws IOException {

        sendDrawing(
                MessageType.SUBMIT_DRAWING,
                roundNumber,
                drawingData
        );
    }

// 작성 중인 그림을 저장할 때 호출
    public void saveDrawingProgress(
            int roundNumber,
            byte[] drawingData) throws IOException {

        sendDrawing(
                MessageType.SAVE_DRAWING_PROGRESS,
                roundNumber,
                drawingData
        );
    }

    private void sendDrawing(
            MessageType type,
            int roundNumber,
            byte[] drawingData) throws IOException {

        if (roundNumber < 1) {
            throw new IllegalArgumentException(
                    "라운드 번호는 1 이상이어야 합니다."
            );
        }

        if (drawingData == null || drawingData.length == 0) {
            throw new IllegalArgumentException(
                    "전송할 그림 데이터가 없습니다."
            );
        }

        Message request = new Message();
        request.setType(type);
        request.setRoundNumber(roundNumber);
        request.setDrawingData(drawingData);

        send(request);
    }
    
    public void submitAnswer(
            int roundNumber,
            String submittedAnswer) throws IOException {

        if (roundNumber < 1) {
            throw new IllegalArgumentException(
                    "라운드 번호는 1 이상이어야 합니다."
            );
        }

        if (submittedAnswer == null || submittedAnswer.isBlank()) {
            throw new IllegalArgumentException(
                    "정답을 입력해야 합니다."
            );
        }

        Message request = new Message();
        request.setType(MessageType.SUBMIT_ANSWER);
        request.setRoundNumber(roundNumber);
        request.setSubmittedAnswer(submittedAnswer);

        send(request);
    }
    
}
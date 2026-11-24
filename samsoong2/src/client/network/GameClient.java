package client.network;

import common.Message;
import common.Protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class GameClient {

    private Socket socket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread receiveThread;
    //private volatile String currentRole;
    private final BlockingQueue<List<String>> categoryQueue = new LinkedBlockingQueue<>();
    private final BlockingQueue<Message> phaseQueue = new LinkedBlockingQueue<>();
    
    private volatile boolean connected = false;
    private volatile boolean closed = false;

    public void connect(String serverIp) throws IOException {
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
        ClientReceiver receiver = new ClientReceiver(reader, this);
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
    	if (closed) {
   	        return;
        }

    	closed = true;
        connected = false;
        
        phaseQueue.offer(new Message());
        
        try {
			if(socket != null) {
				socket.close();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println("연결 종료 오류: " + e.getMessage());
		}
    }
    
    public void receiveCategories(List<String> categories) {
        categoryQueue.offer(List.copyOf(categories));
    }

    public List<String> waitForCategories() throws InterruptedException {
    	while (!closed) {
            List<String> categories = categoryQueue.poll(1, TimeUnit.SECONDS);

            if (categories != null) {
                return categories;
            }
        }

        return null;
    }
    
    /*
    public void setCurrentRole(String role) {
        this.currentRole = role;
    }

    public String getCurrentRole() {
        return currentRole;
    }
    */
    
    public void receivePhaseStarted(Message message) {
        phaseQueue.offer(message);
    }

    public Message waitForPhaseStarted() throws InterruptedException {
    	Message message = phaseQueue.take();
    	if (message.getType() == null) {
            return null;
        }
        return message;
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
}
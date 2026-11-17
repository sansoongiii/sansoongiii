package common;

public class Message {

	private MessageType type;
    private final int roundNumber; 
    private final String data;
    
    // 라운드 번호 필요 없는 메시지 
    public Message(MessageType type, String data) {
    	this(type, 0, data);
	}
    
    // 라운드 번호 필요한 메시지 
    public Message(MessageType type, int roundNumber, String data) {
    	if (type == null) {
    	        throw new IllegalArgumentException(
    	                "메시지 종류가 없습니다."
    	        );
    	   }

    	if (roundNumber < 0) {
    	        throw new IllegalArgumentException(
    	                "라운드 번호는 0 이상이어야 합니다."
    	        );
    	   }
		this.type = type;
		this.roundNumber = roundNumber;
		this.data = data;
	}
    

	public MessageType getType() {
		return type;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public String getData() {
		return data;
	}
	
	
	
}

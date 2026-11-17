package common;

public interface GameMessageSender {
	
	// 특정 참가자 한 명에게 전송 
	void sendTo(String playerId,Message message);
	
	//지정된 참가자들에게 전송
	void sendToPlayers(Iterable<String>playerIds, Message message);
}

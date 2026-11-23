package client.controller;
import common.Message;

public class ViewController {
	// 아직 UI 구현 전이므로 뼈대만 작성 (추상 메서드 형태)
	public void showMainView() {};
	public void showWaitingRoomView(String roomCode, int playerCount, String category) {}; // 방 대기
	
	// 역할별 화면
	public void showTextWritingView(int roundNumber, String answer, long deadlineEpochMillis) {};
	public void showDrawingView(int roundNumber, String textDescription, long deadlineEpochMillis) {};
	public void showAnsweringView(int roundNumber, byte[] drawingData, long deadlineEpochMillis) {};
	
	public void showWaitingPhaseView(String currentPhase, String role) {}; // 게임 중 순서 대기
	public void showResultView(Message resultMessage) {}; // 라운드별 결과 (정답/오답 등)
	public void showFinishView() {}; // 라운드 전체 종료 후 결과 (연장 화면 포함)
	
	public void showErrorAlert(String errorCode, String notice) {};
}

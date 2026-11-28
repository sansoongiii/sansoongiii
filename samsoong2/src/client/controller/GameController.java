package client.controller;

import java.io.IOException;
import java.util.Objects;

import client.view.ConsoleView;
import common.Message;

public class GameController {

	private final ConsoleView consoleView = new ConsoleView();
	private ViewController viewController;
	private ConnectionController connectionController;
	
	private int currentRound;
	
	public int getCurrentRound() {
	    return currentRound;
	}

	public void setViewController(ViewController viewController) {
		this.viewController = Objects.requireNonNull(viewController, "ViewController는 null일 수 없습니다.");
	}
	
	public void setConnectionController(ConnectionController connectionController) {
	    this.connectionController = Objects.requireNonNull(
	    		connectionController,
	            "ConnectionController는 null일 수 없습니다."
	    );
	}
	
	public void submitText(String description) throws IOException {
	    connectionController.submitText(currentRound, description);
	}

	public void submitDrawing(byte[] drawingData) throws IOException {
	    connectionController.submitDrawing(currentRound, drawingData);
	}

	public void submitAnswer(String answer) throws IOException {
	    connectionController.submitAnswer(currentRound, answer);
	}

	public void saveTextProgress(String description) throws IOException {
	    connectionController.saveTextProgress(currentRound, description);
	}

	public void saveDrawingProgress(byte[] drawingData) throws IOException {
	    connectionController.saveDrawingProgress(currentRound, drawingData);
	}

	public void saveAnswerProgress(String answer) throws IOException {
	    connectionController.saveAnswerProgress(currentRound, answer);
	}

	public void continueToNextRound() throws IOException {
	    connectionController.respondToContinue(currentRound, true);
	}

	public void onConnected(String playerId) {
		consoleView.onConnected(playerId);
	}

	public void onCategoriesReceived(Message message) {
		consoleView.onCategoriesReceived(message.getCategories());
		viewController.handleMessage(message);
	}

	public void onRoomUpdated(Message message) {
		consoleView.onRoomUpdated(message.getRoomCode(), message.getPlayerCount(), message.getCategory());
		viewController.handleMessage(message);
	}

	public void onPhaseStarted(Message message) {
	    currentRound = message.getRoundNumber();
	   
		consoleView.onPhaseStarted(message);
		viewController.handleMessage(message);
	}

	public void onRoundResult(Message message) {
		currentRound = message.getRoundNumber();
	    
		consoleView.onRoundResult(message);
		viewController.handleMessage(message);
	}

	public void onContinueStatus(Message message) {
		consoleView.onContinueStatus(message.getAgreedCount(), message.getPlayerCount(), message.getNotice());
		viewController.handleMessage(message);
	}

	public void onGameFinished(Message message) {
		consoleView.onGameFinished(message.getNotice());
		viewController.handleMessage(message);
	}

	public void onRoomLeft(Message message) {
		consoleView.onRoomLeft(message.getNotice());
		viewController.handleMessage(message);
	}

	public void onGameAborted(Message message) {
		consoleView.onGameAborted(message.getNotice());
		viewController.handleMessage(message);
	}

	public void onPostGameOptions(Message message) {
		consoleView.onPostGameOptions(message.getNotice());
		viewController.handleMessage(message);
	}

	public void onWaitingForHost(Message message) {
		consoleView.onWaitingForHost(message.getNotice());
		viewController.handleMessage(message);
	}

	public void onError(Message message) {
		consoleView.onError(message.getErrorCode(), message.getNotice());
		viewController.handleMessage(message);
	}

	public void onDisconnected() {
		consoleView.onDisconnected();
		viewController.closeGui();
	}
}
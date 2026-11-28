package client.controller;

import java.util.List;
import java.util.Objects;

import client.view.ConsoleView;
import common.Message;

public class GameController {

	private final ConsoleView consoleView = new ConsoleView();
	private ViewController viewController;

	public void setViewController(ViewController viewController) {
		this.viewController = Objects.requireNonNull(viewController, "ViewController는 null일 수 없습니다.");
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
		consoleView.onPhaseStarted(message);
		viewController.handleMessage(message);
	}

	public void onRoundResult(Message message) {
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
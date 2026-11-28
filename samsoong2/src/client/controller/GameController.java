package client.controller;

import java.util.List;

import client.view.ConsoleView;
import common.Message;

public class GameController {
	
	private final ConsoleView consoleView = new ConsoleView();
	
	public void onConnected(String playerId) {
		consoleView.onConnected(playerId);
	}
	
	public void onCategoriesReceived(List <String> categories) {
		consoleView.onCategoriesReceived(categories);
	}
	
	public void onRoomUpdated(String roomCode, int playerCount, String category) {
		consoleView.onRoomUpdated(roomCode, playerCount, category);
	}
	
	public void onPhaseStarted(Message message) {
		consoleView.onPhaseStarted(message);
	}
	
	public void onRoundResult(Message message) {
        consoleView.onRoundResult(message);
    }
	
	public void onContinueStatus(int agreedCount, int playerCount, String notice) {
        consoleView.onContinueStatus(agreedCount, playerCount, notice);
    }
	
	public void onGameFinished(String notice) {
	        consoleView.onGameFinished(notice);
	}
	
	public void onRoomLeft(String notice) {
        consoleView.onRoomLeft(notice);
    }
	
	public void onGameAborted(String notice) {
        consoleView.onGameAborted(notice);
    }
	
	public void onPostGameOptions(String notice) {
        consoleView.onPostGameOptions(notice);
    }

    public void onWaitingForHost(String notice) {
        consoleView.onWaitingForHost(notice);
    }

    public void onError(String errorCode, String notice) {
        consoleView.onError(errorCode, notice);
    }

    public void onDisconnected() {
        consoleView.onDisconnected();
    }
}
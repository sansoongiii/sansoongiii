package client.command;

import client.ui.CanvasPanel;

public class EraseCommand {
    private CanvasPanel canvasPanel;
    private int x, y;
    private int eraserSize;
    private boolean clearAll;

    // 부분 지우기
    public EraseCommand(CanvasPanel canvasPanel, int x, int y, int eraserSize) {
        this.canvasPanel = canvasPanel;
        this.x = x;
        this.y = y;
        this.eraserSize = eraserSize;
        this.clearAll = false;
    }

    // 전체 지우기
    public EraseCommand(CanvasPanel canvasPanel, boolean clearAll) {
        this.canvasPanel = canvasPanel;
        this.clearAll = clearAll;
    }

    public void execute() {
        if (canvasPanel == null) return;
        if (clearAll) {
            canvasPanel.clearAll();
        } else {
            canvasPanel.erase(x, y, eraserSize);
        }
    }
}
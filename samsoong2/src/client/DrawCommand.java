package client;

import java.awt.Color;

public class DrawCommand {
    private CanvasPanel canvasPanel;
    private int startX, startY, endX, endY;
    private Color color;
    private int strokeSize;

    public DrawCommand(CanvasPanel canvasPanel, int startX, int startY, int endX, int endY, int strokeSize) {
        this.canvasPanel = canvasPanel;
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.strokeSize = strokeSize;
    }

    public void execute() {
        if (canvasPanel != null) {
            canvasPanel.drawLine(startX, startY, endX, endY, strokeSize);
        }
    }
}
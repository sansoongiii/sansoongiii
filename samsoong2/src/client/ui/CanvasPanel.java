package client.ui;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import client.command.DrawCommand;
import client.command.EraseCommand;

public class CanvasPanel extends JPanel {

    public static final int CANVAS_WIDTH = 700;
    public static final int CANVAS_HEIGHT = 450;

    private final BufferedImage canvasImage;
    private Point lastPoint = null;

    private final int brushSize = 4;
    private boolean eraserMode = false;
    private boolean canDraw = true;

    private Consumer<byte[]> progressListener;
    
    public CanvasPanel() {
        setPreferredSize(new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT));
        setBackground(Color.WHITE);

        canvasImage = new BufferedImage(CANVAS_WIDTH, CANVAS_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        clearAll();

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (!canDraw) return;
                lastPoint = e.getPoint();
                applyTool(lastPoint, lastPoint);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (!canDraw) return;
                Point current = e.getPoint();
                if (lastPoint != null) {
                    applyTool(lastPoint, current);
                }
                lastPoint = current;
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                lastPoint = null;
                saveProgress();
            }
        };

        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private void applyTool(Point p1, Point p2) {
        if (eraserMode) {
            new EraseCommand(this, p2.x, p2.y, brushSize * 3).execute();
        } else {
            new DrawCommand(this, p1.x, p1.y, p2.x, p2.y, brushSize).execute();
        }
    }

    // 펜 그리기
    public void drawLine(int x1, int y1, int x2, int y2, int strokeSize) {
        Graphics2D g2 = canvasImage.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.BLACK); // 색상 고정
        g2.setStroke(new BasicStroke(strokeSize, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.drawLine(x1, y1, x2, y2);
        g2.dispose();
        repaint();
    }

    // 부분 지우개
    public void erase(int x, int y, int eraserSize) {
        Graphics2D g2 = canvasImage.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillOval(x - eraserSize / 2, y - eraserSize / 2, eraserSize, eraserSize);
        g2.dispose();
        repaint();
    }

    // 전체 지우개
    public void clearAll() {
        Graphics2D g2 = canvasImage.createGraphics();
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, CANVAS_WIDTH, CANVAS_HEIGHT);
        g2.dispose();
        lastPoint = null;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvasImage, 0, 0, null);
    }

    // PNG 변환 및 복원
    public byte[] exportPng() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        if (!ImageIO.write(canvasImage, "png", baos)) {
            throw new IOException("PNG 이미지 인코딩 실패");
        }
        return baos.toByteArray();
    }

    public void showPng(byte[] drawingData) throws IOException {
        if (drawingData == null) {
            throw new IOException("그림 데이터가 비어 있습니다.");
        }
        BufferedImage loaded = ImageIO.read(new ByteArrayInputStream(drawingData));
        if (loaded == null) {
            throw new IOException("올바르지 않은 PNG 데이터입니다.");
        }
        clearAll();
        Graphics2D g2 = canvasImage.createGraphics();
        g2.drawImage(loaded, 0, 0, CANVAS_WIDTH, CANVAS_HEIGHT, null);
        g2.dispose();
        repaint();
    }

    // 버튼 연결
    public void usePen() {
        this.eraserMode = false;
    }

    public void useEraser() {
        this.eraserMode = true;
    }

    public void setCanDraw(boolean canDraw) {
        this.canDraw = canDraw;
        this.lastPoint = null;
    }
    
    public void requestClearAll() {
        new EraseCommand(this, true).execute();
        saveProgress();
    }
    
    private void saveProgress() {
        if (progressListener == null || !canDraw) {
            return;
        }

        try {
            progressListener.accept(exportPng());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void setProgressListener(Consumer<byte[]> listener) {
        this.progressListener = listener;
    }
}
package client.ui;

import java.awt.*;
import javax.swing.*;

public class CanvasPanelTest {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("그림판 테스트");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            CanvasPanel canvas = new CanvasPanel();

            JPanel toolbar = new JPanel();
            JButton PenBtn = new JButton("펜");
            JButton eraserBtn = new JButton("지우개");
            JButton clearBtn = new JButton("전체 지우기");
            JButton pngTestBtn = new JButton("PNG 변환");

            eraserBtn.addActionListener(e -> canvas.useEraser());
            PenBtn.addActionListener(e -> canvas.usePen());
            clearBtn.addActionListener(e -> canvas.requestClearAll());

            pngTestBtn.addActionListener(e -> {
                try {
                    byte[] pngBytes = canvas.exportPng();
                    System.out.println("변환된 PNG 크기 (byte): " + pngBytes.length);
                    canvas.clearAll();
                    canvas.showPng(pngBytes);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });

            toolbar.add(PenBtn);
            toolbar.add(eraserBtn);
            toolbar.add(clearBtn);
            toolbar.add(pngTestBtn);

            frame.add(toolbar, BorderLayout.NORTH);
            frame.add(canvas, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
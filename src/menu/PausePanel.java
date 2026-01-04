package menu;

import utils.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class PausePanel extends JPanel {
    private final Runnable onResume;
    private final Runnable onExitToMenu;

    // Estados de hover
    private boolean hoverResume = false;
    private boolean hoverExit = false;

    public PausePanel(Runnable onResume, Runnable onExitToMenu) {
        this.onResume = onResume;
        this.onExitToMenu = onExitToMenu;
        setOpaque(false);

        // Listener de ratón para hover y clicks
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Point p = e.getPoint();
                hoverResume = getResumeBounds().contains(p);
                hoverExit = getExitBounds().contains(p);
                repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                Point p = e.getPoint();
                if (getResumeBounds().contains(p)) {
                    if (onResume != null) onResume.run();
                } else if (getExitBounds().contains(p)) {
                    if (onExitToMenu != null) onExitToMenu.run();
                }
            }
        };
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }

    private Rectangle getResumeBounds() {
        int w = 220, h = 120; // altura aumentada
        int x = (getWidth() - w) / 2;
        int y = getHeight() / 2 - 100;
        return new Rectangle(x, y, w, h);
    }

    private Rectangle getExitBounds() {
        int w = 220, h = 120; // altura aumentada
        int x = (getWidth() - w) / 2;
        int y = getHeight() / 2 + 40;
        return new Rectangle(x, y, w, h);
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();

        // Fondo semitransparente
        g.setColor(new Color(0, 0, 0, 140));
        g.fillRect(0, 0, getWidth(), getHeight());

        // Tarjeta central más alta
        int cardW = 400, cardH = 320;
        int cx = (getWidth() - cardW) / 2;
        int cy = (getHeight() - cardH) / 2;
        g.setColor(new Color(20, 20, 20, 220));
        g.fillRect(cx, cy, cardW, cardH);
        g.setColor(Color.BLACK);
        g.drawRect(cx, cy, cardW, cardH);

        // Letrero PAUSA
        g.setColor(Color.WHITE);
        g.setFont(new Font("SansSerif", Font.BOLD, 32));
        g.drawString("PAUSA", cx + cardW / 2 - 60, cy + 50);

        // Botón Reanudar
        Rectangle resumeRect = getResumeBounds();
        BufferedImage resumeImg = hoverResume ? ResourceManager.reanudar1 : ResourceManager.reanudar;
        if (resumeImg != null) {
            g.drawImage(resumeImg, resumeRect.x, resumeRect.y, resumeRect.width, resumeRect.height, null);
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(resumeRect.x, resumeRect.y, resumeRect.width, resumeRect.height);
            g.setColor(Color.WHITE);
            g.drawString("Reanudar", resumeRect.x + 70, resumeRect.y + 65);
        }

        // Botón Salir
        Rectangle exitRect = getExitBounds();
        BufferedImage exitImg = hoverExit ? ResourceManager.salir1 : ResourceManager.salir;
        if (exitImg != null) {
            g.drawImage(exitImg, exitRect.x, exitRect.y, exitRect.width, exitRect.height, null);
        } else {
            g.setColor(Color.GRAY);
            g.fillRect(exitRect.x, exitRect.y, exitRect.width, exitRect.height);
            g.setColor(Color.WHITE);
            g.drawString("Salir", exitRect.x + 90, exitRect.y + 65);
        }

        g.dispose();
    }
}

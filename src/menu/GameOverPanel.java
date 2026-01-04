package menu;

import utils.ResourceManager;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class GameOverPanel extends JPanel {

    public GameOverPanel(Runnable onRestart, Runnable onExit) {
        setLayout(new BorderLayout());
        setOpaque(true);

        // "GAME OVER" centrado
        JLabel titleLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 96));
        titleLabel.setForeground(new Color(220, 40, 40));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(100, 0, 0, 0));
        add(titleLabel, BorderLayout.CENTER);

        // Panel para botones
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 50, 20));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 100, 0));

        // Botón Reintentar con hover
        JButton restartButton = createImageButton(
                ResourceManager.reintentar,
                ResourceManager.reintentar1,
                () -> {
                    if (onRestart != null) {
                        onRestart.run();
                    }
                }
        );

        // Botón Salir con hover
        JButton exitButton = createImageButton(
                ResourceManager.salir,
                ResourceManager.salir1,
                () -> {
                    if (onExit != null) {
                        onExit.run();
                    }
                }
        );

        buttonPanel.add(restartButton);
        buttonPanel.add(exitButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Dibujar fondo "lostPanel"
        if (ResourceManager.lostPanel != null) {
            g.drawImage(ResourceManager.lostPanel, 0, 0, getWidth(), getHeight(), this);
        }
    }

    // Método reutilizable para crear botones con hover
    private JButton createImageButton(BufferedImage normalImg, BufferedImage hoverImg, Runnable action) {
        if (normalImg == null || hoverImg == null) {
            System.err.println("Error: imagen del botón no cargada correctamente en ResourceManager");
            JButton fallback = new JButton("Botón");
            fallback.addActionListener(e -> action.run());
            return fallback;
        }

        ImageIcon normalIcon = new ImageIcon(normalImg);
        ImageIcon hoverIcon = new ImageIcon(hoverImg);

        JButton button = new JButton(normalIcon);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));

        button.addActionListener(e -> action.run());

        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setIcon(hoverIcon);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setIcon(normalIcon);
            }
        });

        return button;
    }
}

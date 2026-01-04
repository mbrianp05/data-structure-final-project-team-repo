package menu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import utils.ResourceManager;

public class VictoryPanel extends JPanel {

    public VictoryPanel(Runnable onBackToMenu) {
        setLayout(new BorderLayout());

        // Panel principal con contenido
        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);

        // Panel para el mensaje de victoria
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(false);
        messagePanel.setBorder(new EmptyBorder(80, 0, 0, 0));

        JLabel victoryLabel = new JLabel("¡VICTORIA!", SwingConstants.CENTER);
        victoryLabel.setFont(new Font("Arial", Font.BOLD, 72));
        victoryLabel.setForeground(new Color(255, 215, 0)); // Dorado
        victoryLabel.setOpaque(false);

        victoryLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.BLACK, 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        messagePanel.add(victoryLabel, BorderLayout.CENTER);
        contentPanel.add(messagePanel, BorderLayout.CENTER);

        // Panel para el botón (parte inferior)
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(0, 0, 100, 0));

        // Botón único de salir con hover
        JButton exitButton = createImageButton(ResourceManager.salir, ResourceManager.salir1, () -> {
            if (onBackToMenu != null) {
                onBackToMenu.run(); // vuelve directo al menú
            }
        });

        buttonPanel.add(exitButton);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(contentPanel, BorderLayout.CENTER);
    }

    // Método para crear botones con imágenes y hover
    private JButton createImageButton(BufferedImage normalImg, BufferedImage hoverImg, Runnable action) {
        if (normalImg == null || hoverImg == null) {
            System.err.println("Error: imagen del botón no cargada correctamente en ResourceManager");
            JButton fallback = new JButton("Salir");
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

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (ResourceManager.victoryPanel != null) {
            g.drawImage(ResourceManager.victoryPanel, 0, 0, getWidth(), getHeight(), this);
        } else {
            g.setColor(new Color(20, 20, 40));
            g.fillRect(0, 0, getWidth(), getHeight());

            g.setColor(new Color(40, 40, 80));
            for (int i = 0; i < getWidth(); i += 20) {
                for (int j = 0; j < getHeight(); j += 20) {
                    g.fillRect(i, j, 10, 10);
                }
            }

            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 16));
            String msg = "Imagen victoryPanel no encontrada";
            int msgWidth = g.getFontMetrics().stringWidth(msg);
            g.drawString(msg, (getWidth() - msgWidth) / 2, getHeight() / 2);
        }

        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, getWidth(), getHeight());
        g2d.dispose();
    }

    public static boolean isBackgroundImageLoaded() {
        return ResourceManager.victoryPanel != null;
    }
}

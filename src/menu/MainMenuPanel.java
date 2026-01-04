package menu;

import utils.ResourceManager;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class MainMenuPanel {
    private JFrame frame;

    public void showMenu() {
        ResourceManager.init();

        frame = new JFrame("Forest Survivors - Menú");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1024, 768); // resolución actualizada
        frame.setLocationRelativeTo(null);

        // Panel principal con fondo dibujado
        JPanel main = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                BufferedImage fondo = ResourceManager.fondo1;
                if (fondo != null) {
                    g.drawImage(fondo, 0, 0, getWidth(), getHeight(), null);
                } else {
                    g.setColor(Color.DARK_GRAY);
                    g.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        main.setLayout(new BorderLayout());

        // Panel central con botones
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));
        center.setBorder(BorderFactory.createEmptyBorder(420, 40, 40, 40)); // margen superior

        JButton start = createImageButton(ResourceManager.iniciar, ResourceManager.iniciar1, () -> {
            frame.dispose();
            GameWindow gw = new GameWindow();
            gw.showGameWindow();
        });

        JButton exit = createImageButton(ResourceManager.salir, ResourceManager.salir1, () -> {
            System.exit(0);
        });

        center.add(Box.createVerticalGlue());
        // espacio extra solo antes del botón Iniciar
        center.add(Box.createVerticalStrut(30));
        center.add(start);
        center.add(Box.createVerticalStrut(20));
        center.add(exit);
        center.add(Box.createVerticalGlue());

        main.add(center, BorderLayout.CENTER);

        frame.setContentPane(main);
        frame.setVisible(true);
    }

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
        button.setAlignmentX(Component.CENTER_ALIGNMENT);

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

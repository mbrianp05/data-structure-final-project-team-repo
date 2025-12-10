package menu;

import javax.swing.*;
import java.awt.*;

public class GameOverPanel extends JPanel {
    public GameOverPanel(Runnable onRestart, Runnable onExit) {
        setLayout(new BorderLayout());
        setBackground(new Color(40, 20, 20));

        // Title panel
        JPanel titlePanel = new JPanel();
        titlePanel.setOpaque(false);
        titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel("GAME OVER", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
        titleLabel.setForeground(new Color(200, 40, 40));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel subtitleLabel = new JLabel("Has sido derrotado", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        subtitleLabel.setForeground(Color.WHITE);
        subtitleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        titlePanel.add(Box.createVerticalStrut(60));
        titlePanel.add(titleLabel);
        titlePanel.add(Box.createVerticalStrut(20));
        titlePanel.add(subtitleLabel);

        add(titlePanel, BorderLayout.CENTER);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        buttonPanel.setOpaque(false);

        JButton restartButton = new JButton("Reintentar");
        restartButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        restartButton.setPreferredSize(new Dimension(160, 50));
        restartButton.setBackground(new Color(80, 120, 80));
        restartButton.setForeground(Color.WHITE);
        restartButton.setFocusPainted(false);
        restartButton.addActionListener(e -> {
            if (onRestart != null)
                onRestart.run();
        });

        JButton exitButton = new JButton("Salir al Menú");
        exitButton.setFont(new Font("SansSerif", Font.BOLD, 18));
        exitButton.setPreferredSize(new Dimension(160, 50));
        exitButton.setBackground(new Color(120, 60, 60));
        exitButton.setForeground(Color.WHITE);
        exitButton.setFocusPainted(false);
        exitButton.addActionListener(e -> {
            if (onExit != null)
                onExit.run();
        });

        buttonPanel.add(restartButton);
        buttonPanel.add(exitButton);

        add(buttonPanel, BorderLayout.SOUTH);
    }
}

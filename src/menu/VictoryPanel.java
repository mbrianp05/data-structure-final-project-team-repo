package menu;

import javax.swing.*;
import java.awt.*;

public class VictoryPanel extends JPanel {
    public VictoryPanel(Runnable onBackToMenu) {
        setLayout(new BorderLayout());
        setBackground(new Color(28, 40, 20));

        JLabel lbl = new JLabel("¡Victoria!", SwingConstants.CENTER);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 28));
        lbl.setForeground(Color.WHITE);
        lbl.setBorder(BorderFactory.createEmptyBorder(36, 12, 12, 12));
        add(lbl, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        JButton menu = new JButton("Volver al menú");
        menu.addActionListener(e -> {
            if (onBackToMenu != null)
                onBackToMenu.run();
        });
        JButton exit = new JButton("Salir");
        exit.addActionListener(e -> System.exit(0));
        bottom.add(menu);
        bottom.add(exit);
        add(bottom, BorderLayout.SOUTH);
    }
}

import menu.MainMenuPanel;
import javax.swing.SwingUtilities;
public class Main {
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            MainMenuPanel menu = new MainMenuPanel();
            menu.showMenu();
        });
    }
}

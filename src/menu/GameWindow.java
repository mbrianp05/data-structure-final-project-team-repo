package menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import Tree.BinaryTree;
import Tree.BinaryTreeNode;
import game.GameController;
import game.GameEventListener;
import game.GamePanel;
import map.MineRoom;
import map.SimpleMapBuilder;

/**
 * menu.GameWindow: crea el mapa, controlador y game.GamePanel; escucha eventos
 * de game.GameController.
 * Añade pausa con ESC que realmente detiene/reanuda el loop del game.GamePanel.
 */
public class GameWindow implements GameEventListener {
    private JFrame frame;
    private GamePanel gamePanel;
    private GameController controller;

    // overlay de pausa
    private PausePanel pausePanel;
    private boolean isPaused = false;

    public void showGameWindow() {
        // construir mapa y controlador: ajusta height/size si lo necesitas
        BinaryTree<MineRoom> map = SimpleMapBuilder.buildProceduralBinaryMap(3, 800, 600);
        BinaryTreeNode<MineRoom> start = SimpleMapBuilder.pickRandomLeaf(map);
        if (start == null) {
            Tree.TreeNode<MineRoom> root = map.getRoot();
            if (root instanceof BinaryTreeNode)
                start = (BinaryTreeNode<MineRoom>) root;
        }

        controller = new GameController(map, start);
        controller.setGameEventListener(this);

        // Crear game.GamePanel pasando el controller. Ajusta si tu constructor es
        // diferente.
        gamePanel = new GamePanel(controller);

        frame = new JFrame("Mi Juego");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        // agregar el panel del juego
        frame.add(gamePanel, BorderLayout.CENTER);

        // Configurar map.Key Bindings en el game.GamePanel para ESC (más fiable que
        // KeyListener en el frame)
        setupPauseKeyBinding();

        frame.setVisible(true);

        // Si game.GamePanel necesita inicio explícito de loop, invoca start/initialize
        // aquí.
        invokeIfExists(gamePanel, "startLoop");
    }

    // configura map.Key Binding en gamePanel: ESC para toggle pausa
    private void setupPauseKeyBinding() {
        // usar WHEN_IN_FOCUSED_WINDOW para que funcione aunque gamePanel no tenga focus
        // exacto
        InputMap im = gamePanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = gamePanel.getActionMap();

        im.put(KeyStroke.getKeyStroke("ESCAPE"), "togglePause");
        am.put("togglePause", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                togglePause();
            }
        });
    }

    private void togglePause() {
        if (!isPaused)
            showPauseOverlay();
        else
            hidePauseOverlay();
    }

    private void showPauseOverlay() {
        if (isPaused || frame == null || gamePanel == null)
            return;
        isPaused = true;

        // Primero, pausar el game.GamePanel (detiene Timer y notifica al controlador)
        try {
            Method m = gamePanel.getClass().getMethod("pause");
            m.invoke(gamePanel);
        } catch (NoSuchMethodException nsme) {
            // fallback: intentar stopLoop si pause() no existe
            invokeIfExists(gamePanel, "stopLoop");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // crear overlay (menu.PausePanel) con callbacks
        pausePanel = new PausePanel(
                () -> { // onResume
                    hidePauseOverlay();
                },
                () -> { // onExitToMenu
                    cleanupAndReturnToMenu();
                });

        // agregar overlay al LayeredPane para aparecer encima del game.GamePanel
        JLayeredPane layered = frame.getLayeredPane();
        layered.add(pausePanel, JLayeredPane.POPUP_LAYER);

        // fijar tamaño y posición del overlay para cubrir la ventana de contenido
        // usamos getContentPane().getBounds() y convertimos a coordenadas de
        // layeredPane
        Rectangle contentBounds = frame.getContentPane().getBounds();
        Point contentLoc = SwingUtilities.convertPoint(frame.getContentPane(), 0, 0, layered);
        pausePanel.setBounds(contentLoc.x, contentLoc.y, contentBounds.width, contentBounds.height);

        pausePanel.revalidate();
        pausePanel.repaint();

        // asegurar foco para que se siga capturando ESC y que input vuelva a
        // game.GamePanel al reanudar
        frame.requestFocusInWindow();
    }

    private void hidePauseOverlay() {
        if (!isPaused || frame == null || gamePanel == null)
            return;
        isPaused = false;

        // remover overlay PRIMERO para liberar foco y componentes
        if (pausePanel != null) {
            frame.getLayeredPane().remove(pausePanel);
            frame.getLayeredPane().revalidate();
            frame.getLayeredPane().repaint();
            pausePanel = null;
        }

        // ahora reanudar game.GamePanel (resume() si existe; fallback a startLoop)
        try {
            Method m = gamePanel.getClass().getMethod("resume");
            m.invoke(gamePanel);
        } catch (NoSuchMethodException nsme) {
            invokeIfExists(gamePanel, "startLoop");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // garantizar que el game.GamePanel reciba foco (usar invokeLater para que Swing
        // procese la remoción del overlay)
        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
            frame.requestFocusInWindow();
        });

        System.out.println("menu.GameWindow: pausa ocultada (overlay removido, panel reanudado)");
    }

    private void cleanupAndReturnToMenu() {
        // detener/limpiar el panel del juego
        invokeIfExists(gamePanel, "stopLoop");

        if (frame != null)
            frame.dispose();

        SwingUtilities.invokeLater(() -> {
            MainMenuPanel menu = new MainMenuPanel();
            menu.showMenu();
        });
    }

    @Override
    public void onWin() {
        // si gana estando pausado, cerrar overlay y mostrar victory
        SwingUtilities.invokeLater(() -> {
            if (isPaused)
                hidePauseOverlay();
            showVictoryPanel();
        });
    }

    @Override
    public void onGameOver() {
        // mostrar panel de game over
        SwingUtilities.invokeLater(() -> {
            if (isPaused)
                hidePauseOverlay();
            showGameOverPanel();
        });
    }

    @Override
    public void onExitToMenu() {
        SwingUtilities.invokeLater(this::cleanupAndReturnToMenu);
    }

    private void showVictoryPanel() {
        // intentar detener loop del game.GamePanel
        invokeIfExists(gamePanel, "stopLoop");

        Container c = frame.getContentPane();
        c.removeAll();

        VictoryPanel vp = new VictoryPanel(() -> {
            cleanupAndReturnToMenu();
        });

        c.add(vp, BorderLayout.CENTER);
        c.revalidate();
        c.repaint();
    }

    private void showGameOverPanel() {
        // detener loop del game.GamePanel
        invokeIfExists(gamePanel, "stopLoop");

        Container c = frame.getContentPane();
        c.removeAll();

        GameOverPanel gop = new GameOverPanel(
                () -> { // onRestart
                    restartGame();
                },
                () -> { // onExit
                    cleanupAndReturnToMenu();
                });

        c.add(gop, BorderLayout.CENTER);
        c.revalidate();
        c.repaint();
    }

    private void restartGame() {
        // cerrar ventana actual
        if (frame != null) {
            frame.dispose();
        }

        // crear nueva ventana de juego
        SwingUtilities.invokeLater(() -> {
            GameWindow newGame = new GameWindow();
            newGame.showGameWindow();
        });
    }

    // utilitario: invoca un método sin argumentos por reflexión si existe
    private void invokeIfExists(Object target, String methodName) {
        if (target == null)
            return;
        try {
            Method m = target.getClass().getMethod(methodName);
            if (m != null)
                m.invoke(target);
        } catch (NoSuchMethodException nsme) {
            // método no existe: ignorar
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // util: intenta pause/resume y si no existe usa startLoop/stopLoop
    private void pauseGamePanel() throws InvocationTargetException, IllegalAccessException {
        if (gamePanel == null)
            return;
        try {
            java.lang.reflect.Method m = gamePanel.getClass().getMethod("pause");
            m.invoke(gamePanel);
            return;
        } catch (NoSuchMethodException nsme) {
        }
        // fallback a stopLoop
        invokeIfExists(gamePanel, "stopLoop");
    }

    private void resumeGamePanel() {
        if (gamePanel == null)
            return;
        try {
            java.lang.reflect.Method m = gamePanel.getClass().getMethod("resume");
            m.invoke(gamePanel);
        } catch (NoSuchMethodException nsme) {
            // fallback a startLoop
            invokeIfExists(gamePanel, "startLoop");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // reset input state to avoid stuck keys
        try {
            java.lang.reflect.Method r = gamePanel.getClass().getMethod("resetInputState");
            r.invoke(gamePanel);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException nsme) {
        }
        // ensure focus
        gamePanel.requestFocusInWindow();
    }

}

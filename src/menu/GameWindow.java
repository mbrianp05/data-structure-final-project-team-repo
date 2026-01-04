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

public class GameWindow implements GameEventListener {
    private JFrame frame;
    private GamePanel gamePanel;
    private GameController controller;

    private PausePanel pausePanel;
    private boolean isPaused = false;

    public void showGameWindow() {
        BinaryTree<MineRoom> map = SimpleMapBuilder.buildProceduralBinaryMap(3, 800, 600);
        BinaryTreeNode<MineRoom> start = SimpleMapBuilder.pickRandomLeaf(map);
        if (start == null) {
            Tree.TreeNode<MineRoom> root = map.getRoot();
            if (root instanceof BinaryTreeNode) {
                start = (BinaryTreeNode<MineRoom>) root;
            }
        }

        controller = new GameController(map, start);
        controller.setGameEventListener(this);

        gamePanel = new GamePanel(controller);

        frame = new JFrame("Mi Juego");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setSize(1024, 768);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new BorderLayout());

        frame.add(gamePanel, BorderLayout.CENTER);

        setupPauseKeyBinding();

        frame.setVisible(true);

        invokeIfExists(gamePanel, "startLoop");
    }

    private void setupPauseKeyBinding() {
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
        if (!isPaused) {
            showPauseOverlay();
        }
        else {
            hidePauseOverlay();
        }
    }

    private void showPauseOverlay() {
        if (isPaused || frame == null || gamePanel == null) {
            return;
        }
        isPaused = true;

        try {
            Method m = gamePanel.getClass().getMethod("pause");
            m.invoke(gamePanel);
        } catch (NoSuchMethodException nsme) {
            invokeIfExists(gamePanel, "stopLoop");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        pausePanel = new PausePanel(
                () -> {
                    hidePauseOverlay();
                },
                () -> {
                    cleanupAndReturnToMenu();
                });

        JLayeredPane layered = frame.getLayeredPane();
        layered.add(pausePanel, JLayeredPane.POPUP_LAYER);

        Rectangle contentBounds = frame.getContentPane().getBounds();
        Point contentLoc = SwingUtilities.convertPoint(frame.getContentPane(), 0, 0, layered);
        pausePanel.setBounds(contentLoc.x, contentLoc.y, contentBounds.width, contentBounds.height);

        pausePanel.revalidate();
        pausePanel.repaint();

        frame.requestFocusInWindow();
    }

    private void hidePauseOverlay() {
        if (!isPaused || frame == null || gamePanel == null) {
            return;
        }
        isPaused = false;

        if (pausePanel != null) {
            frame.getLayeredPane().remove(pausePanel);
            frame.getLayeredPane().revalidate();
            frame.getLayeredPane().repaint();
            pausePanel = null;
        }

        try {
            Method m = gamePanel.getClass().getMethod("resume");
            m.invoke(gamePanel);
        } catch (NoSuchMethodException nsme) {
            invokeIfExists(gamePanel, "startLoop");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            gamePanel.requestFocusInWindow();
            frame.requestFocusInWindow();
        });


    }

    private void cleanupAndReturnToMenu() {
        invokeIfExists(gamePanel, "stopLoop");

        if (frame != null) {
            frame.dispose();
        }

        SwingUtilities.invokeLater(() -> {
            MainMenuPanel menu = new MainMenuPanel();
            menu.showMenu();
        });
    }

    @Override
    public void onWin() {
        SwingUtilities.invokeLater(() -> {
            if (isPaused) {
                hidePauseOverlay();
            }
            showVictoryPanel();
        });
    }

    @Override
    public void onGameOver() {
        SwingUtilities.invokeLater(() -> {
            if (isPaused) {
                hidePauseOverlay();
            }
            showGameOverPanel();
        });
    }

    @Override
    public void onExitToMenu() {
        SwingUtilities.invokeLater(this::cleanupAndReturnToMenu);
    }

    private void showVictoryPanel() {
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
        invokeIfExists(gamePanel, "stopLoop");

        Container c = frame.getContentPane();
        c.removeAll();

        GameOverPanel gop = new GameOverPanel(
                () -> {
                    restartGame();
                },
                () -> {
                    cleanupAndReturnToMenu();
                });

        c.add(gop, BorderLayout.CENTER);
        c.revalidate();
        c.repaint();
    }

    private void restartGame() {
        if (frame != null) {
            frame.dispose();
        }

        SwingUtilities.invokeLater(() -> {
            GameWindow newGame = new GameWindow();
            newGame.showGameWindow();
        });
    }

    private void invokeIfExists(Object target, String methodName) {
        if (target == null) {
            return;
        }
        try {
            Method m = target.getClass().getMethod(methodName);
            if (m != null) {
                m.invoke(target);
            }
        } catch (NoSuchMethodException nsme) {
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void pauseGamePanel() throws InvocationTargetException, IllegalAccessException {
        if (gamePanel == null) {
            return;
        }
        try {
            java.lang.reflect.Method m = gamePanel.getClass().getMethod("pause");
            m.invoke(gamePanel);
        } catch (NoSuchMethodException nsme) {
            invokeIfExists(gamePanel, "stopLoop");
        }
    }

    private void resumeGamePanel() {
        if (gamePanel == null) {
            return;
        }
        try {
            java.lang.reflect.Method m = gamePanel.getClass().getMethod("resume");
            m.invoke(gamePanel);
        } catch (NoSuchMethodException nsme) {
            invokeIfExists(gamePanel, "startLoop");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try {
            java.lang.reflect.Method r = gamePanel.getClass().getMethod("resetInputState");
            r.invoke(gamePanel);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException nsme) {
        }
        gamePanel.requestFocusInWindow();
    }
}
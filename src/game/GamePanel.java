package game;

import Tree.BinaryTreeNode;
import entities.Enemy;
import entities.Player;
import map.Crystal;
import map.Door;
import map.Key;
import map.MineRoom;
import menu.Choice;
import utils.ResourceManager;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.List;

import static utils.ResourceManager.*;

/**
 * game.GamePanel: panel principal del juego.
 * - Ejecuta el loop (Swing Timer) y llama a game.GameController.update(dt,...)
 * - Dibuja escena básica, barra de XP y modal de nivel
 * - Maneja entrada de teclado para movimiento y click en modal
 * <p>
 * Requiere que tengas implementadas las clases: game.GameController,
 * entities.Player, entities.Enemy, entities.EnemyManager, menu.PerkPool,
 * menu.Choice, etc.
 * Ajusta constantes (WIDTH, HEIGHT, FPS) según tu proyecto y renderer.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener, MouseListener, MouseMotionListener {
    public static final int WIDTH = 1024;
    public static final int HEIGHT = 720;
    private static final int FPS = 60;

    private final Timer timer;
    private final GameController controller;

    private boolean up, down, left, right;
    private Point mousePos = new Point(0, 0);

    private long lastTimeNs = System.nanoTime();

    private volatile boolean paused = false;

    public GamePanel(GameController controller) {

        this.controller = controller;

        try {
            ResourceManager.init();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocusInWindow();

        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocusInWindow();

        addMouseListener(this);
        addMouseMotionListener(this);

        InputMap im = this.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = this.getActionMap();

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, false), "up-press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, false), "down-press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, false), "left-press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, false), "right-press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, false), "up-press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, false), "down-press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, false), "left-press");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, false), "right-press");

        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_W, 0, true), "up-release");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0, true), "down-release");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, 0, true), "left-release");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_D, 0, true), "right-release");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0, true), "up-release");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0, true), "down-release");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0, true), "left-release");
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0, true), "right-release");

        am.put("up-press", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GamePanel.this.up = true;
                GamePanel.this.updatePlayerFacingFromKeys();
            }
        });
        am.put("up-release", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GamePanel.this.up = false;
                GamePanel.this.updatePlayerFacingFromKeys();
            }
        });
        am.put("down-press", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GamePanel.this.down = true;
                GamePanel.this.updatePlayerFacingFromKeys();
            }
        });
        am.put("down-release", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GamePanel.this.down = false;
                GamePanel.this.updatePlayerFacingFromKeys();
            }
        });
        am.put("left-press", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GamePanel.this.left = true;
                controller.player.setFacingLeft(); // <-- aquí debe ser izquierda
                GamePanel.this.updatePlayerFacingFromKeys();
            }
        });

        am.put("left-release", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GamePanel.this.left = false;
                GamePanel.this.updatePlayerFacingFromKeys();
            }
        });

        am.put("right-press", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GamePanel.this.right = true;
                controller.player.setFacingRight(); // correcto: derecha
                GamePanel.this.updatePlayerFacingFromKeys();
            }
        });

        am.put("right-release", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                GamePanel.this.right = false;
                GamePanel.this.updatePlayerFacingFromKeys();
            }
        });
        timer = new Timer(1000 / FPS, this);
        timer.setCoalesce(true);
        timer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.nanoTime();
        float dt = Math.max(1f / 120f, (now - lastTimeNs) / 1_000_000_000f);
        lastTimeNs = now;

        System.out.println("TICK paused=" + paused + " levelUpModal=" + controller.isLevelUpModalOpen());

        if (!paused) {
            if (!controller.isLevelUpModalOpen()) {
                applyPlayerInput(dt);
            } else {
                System.out.println("Skipping applyPlayerInput because levelUpModalOpen == true");
            }
            controller.update(dt, null);
        }

        repaint();
    }

    private void applyPlayerInput(float dt) {
        Player p = controller.player;
        float beforeX = p.x, beforeY = p.y;
        float moveSpeed = 140f * p.moveSpeedMultiplier;
        float vx = 0f, vy = 0f;
        if (left)
            vx -= moveSpeed;
        if (right)
            vx += moveSpeed;
        if (up)
            vy -= moveSpeed;
        if (down)
            vy += moveSpeed;
        p.x += vx * dt;
        p.y += vy * dt;
        BinaryTreeNode<MineRoom> node = controller.nodoActual;
        if (node != null) {
            MineRoom r = node.getInfo();
            if (r != null) {
                p.x = Math.max(8f, Math.min(r.width - 8f, p.x));
                p.y = Math.max(8f, Math.min(r.height - 8f, p.y));
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();

        int w = getWidth();
        int h = getHeight();

        // --- Dibujar fondo base (color oscuro) ---
        g.setColor(new Color(24, 30, 20));
        g.fillRect(0, 0, w, h);

        // --- Dibujar pared como background ---
        if (wall != null) {
            g.drawImage(wall, 0, 0, w, h, null);
        }

        // --- Dibujar room background + doors ---
        drawRoomBackground(g);

        // --- Dibujar enemigos ---
        drawEnemies(g);

        // --- Dibujar player ---
        drawPlayer(g);

        // --- Dibujar crystals y keys ---
        drawCrystals(g);
        drawKeys(g);

        // --- Dibujar projectiles ---
        controller.renderProjectiles(g, w, h);

        // --- Dibujar XP bar ---
        drawXpBar(g, w, h);

        // --- Dibujar level-up modal ---
        if (controller.isLevelUpModalOpen()) {
            drawLevelUpModal(g, w, h);
        }

        g.dispose();
    }

    private void drawRoomBackground(Graphics2D g) {
        BinaryTreeNode<MineRoom> node = controller.nodoActual;
        if (node == null)
            return;
        MineRoom r = node.getInfo();
        if (r == null)
            return;

        int roomW = r.width;
        int roomH = r.height;
        int ox = (getWidth() - roomW) / 2;
        int oy = (getHeight() - roomH) / 2 - 20;

        g.translate(ox, oy);

        // --- Seleccionar sprite de piso según tipo definido en la sala ---
        BufferedImage chosenFloor = switch (r.floorType) {
            case 1 -> floor;
            default -> floor;
        };

        // --- Dibujar suelo ---
        if (chosenFloor != null) {
            int tileW = chosenFloor.getWidth();
            int tileH = chosenFloor.getHeight();
            for (int x = 0; x < roomW; x += tileW) {
                for (int y = 0; y < roomH; y += tileH) {
                    int w = Math.min(tileW, roomW - x);
                    int h = Math.min(tileH, roomH - y);
                    g.drawImage(chosenFloor, x, y, x + w, y + h, 0, 0, w, h, null);
                }
            }
        } else {
            g.setColor(new Color(50, 40, 34));
            g.fillRect(0, 0, roomW, roomH);
        }

        // --- Dibujar puertas ---
        for (Door d : r.doors) {
            Rectangle a = d.area;
            BufferedImage sprite = null;

            if (d.isWin) {
                sprite = finaldoor;
            } else {
                sprite = switch (d.tipo) {
                    case 1 -> p1;
                    case 2 -> p2;
                    case 3 -> p3;
                    case 4 -> p4;
                    case 5 -> p5;
                    default -> p1;
                };
            }

            if (sprite != null) {
                g.drawImage(sprite, a.x, a.y, a.width, 90, null);
            } else {
                // fallback visual si no hay sprite
                g.setColor(d.isWin ? new Color(80, 30, 30) : new Color(120, 90, 70));
                g.fillRect(a.x, a.y, a.width, a.height);
                g.setColor(Color.BLACK);
                g.drawRect(a.x, a.y, a.width, a.height);
            }
        }

        // --- Borde de la sala ---
        g.setColor(new Color(59, 54, 50));
        g.drawRect(0, 0, roomW - 1, roomH - 1);

        g.translate(-ox, -oy);
    }

    private void drawEnemies(Graphics2D g) {
        BinaryTreeNode<MineRoom> node = controller.nodoActual;
        if (node == null)
            return;
        MineRoom r = node.getInfo();
        if (r == null)
            return;

        int roomW = r.width;
        int roomH = r.height;
        int ox = (getWidth() - roomW) / 2;
        int oy = (getHeight() - roomH) / 2 - 20;

        java.util.List<Enemy> enemies = controller.enemyManager.getEnemiesAt(node);
        g.translate(ox, oy);

        for (Enemy en : enemies) {
            if (en == null || !en.isAlive())
                continue;

            float ex = en.getX();
            float ey = en.getY();
            BufferedImage sprite = null;

            if (en.level == 1) {
                // usar el id del enemigo para decidir si es slime o bat
                boolean isBat = (en.getId() % 2 == 0); // mitad serán bats, mitad slimes

                if (!isBat) {
                    // --- Slime: animación simple y dirección ---
                    sprite = ResourceManager.slime;
                    if (en.getAnimTick() / 20 % 2 == 1) {
                        sprite = en.isFacingLeft() ? ResourceManager.slimeI : ResourceManager.slimeD;
                    }
                } else {
                    // --- Bat: animación cíclica de 3 frames ---
                    int frame = (en.getAnimTick() / 15) % 3;
                    sprite = switch (frame) {
                        case 0 -> ResourceManager.bat1;
                        case 1 -> ResourceManager.bat2;
                        default -> ResourceManager.bat3;
                    };
                }

            } else if (en.level == 2) {
                // --- Goblin: animación cíclica de 4 frames ---
                int frame = (en.getAnimTick() / 15) % 4;
                sprite = switch (frame) {
                    case 0 -> ResourceManager.gob1;
                    case 1 -> ResourceManager.gob2;
                    case 2 -> ResourceManager.gob3;
                    default -> ResourceManager.gob1;
                };
            } else if (en.level == 3) {
                // --- Esqueleto: animación cíclica de 4 frames ---
                int frame = (en.getAnimTick() / 15) % 4;
                sprite = switch (frame) {
                    case 0 -> ResourceManager.esq1;
                    case 1 -> ResourceManager.esq2;
                    case 2 -> ResourceManager.esq3;
                    default -> ResourceManager.esq1;
                };
            }

            if (sprite != null) {
                int drawW = sprite.getWidth();
                int drawH = sprite.getHeight();
                int dx = (int) (ex - drawW / 2f);
                int dy = (int) (ey - drawH / 2f);
                g.drawImage(sprite, dx, dy, drawW, drawH, null);
            }

            // --- Barra de vida encima del enemigo ---
            int bw = 30, bh = 4;
            int barX = (int) (ex - bw / 2);
            int barY = (int) (ey - 20);
            float hpFrac = en.getMaxHp() > 0 ? Math.max(0f, Math.min(1f, (float) en.getHp() / en.getMaxHp())) : 0f;

            g.setColor(new Color(0, 0, 0, 160));
            g.fillRect(barX, barY, bw, bh);
            g.setColor(new Color(200, 40, 40));
            g.fillRect(barX, barY, (int) (bw * hpFrac), bh);
        }

        g.translate(-ox, -oy);
    }

    private void drawPlayer(Graphics2D g) {
        BinaryTreeNode<MineRoom> node = controller.nodoActual;
        if (node == null)
            return;
        MineRoom r = node.getInfo();
        if (r == null)
            return;

        int roomW = r.width;
        int roomH = r.height;
        int ox = (getWidth() - roomW) / 2;
        int oy = (getHeight() - roomH) / 2 - 20;
        Player p = controller.player;
        if (p == null)
            return;

        g.translate(ox, oy);

        // intentamos dibujar el sprite cargado; si no existe, fallback al rectángulo
        BufferedImage img = ResourceManager.player;
        if (img != null) {
            BufferedImage sprite = null;

            int frame = (p.getAnimTick() / 15) % 3; // ciclo de 3 frames

            if (p.isFacingLeft()) {
                sprite = switch (frame) {
                    case 0 -> ResourceManager.devil1I;
                    case 1 -> ResourceManager.devil2I;
                    case 2 -> ResourceManager.devil3I;
                    default -> ResourceManager.devil1I;
                };
            } else { // mirando a la derecha
                sprite = switch (frame) {
                    case 0 -> ResourceManager.devil1D;
                    case 1 -> ResourceManager.devil2D;
                    case 2 -> ResourceManager.devil3D;
                    default -> ResourceManager.devil1D;
                };
            }

            if (sprite != null) {
                int drawW = sprite.getWidth();
                int drawH = sprite.getHeight();
                int dx = (int) (p.x - drawW / 2f);
                int dy = (int) (p.y - drawH / 2f);
                g.drawImage(sprite, dx, dy, drawW, drawH, null);
            }
        } else {
            g.setColor(new Color(200, 180, 120));
            g.fillRect((int) (p.x - p.w / 2), (int) (p.y - p.h / 2), p.w, p.h);
        }

        // HP barra
        int bw = 100, bh = 8, bx = 8, by = 8;
        g.setColor(new Color(20, 20, 20, 180));
        g.fillRect(bx, by, bw, bh);

        float hpFrac = p.maxHp > 0 ? (float) p.hp / p.maxHp : 0f;
        g.setColor(new Color(200, 40, 40));
        g.fillRect(bx, by, Math.max(0, (int) (bw * hpFrac)), bh);
        g.setColor(Color.WHITE);
        g.drawString("HP: " + p.hp + "/" + p.maxHp, bx + 4, by + bh - 1);

        g.translate(-ox, -oy);
    }

    private void drawCrystals(Graphics2D g) {
        BinaryTreeNode<MineRoom> node = controller.nodoActual;
        if (node == null)
            return;
        MineRoom r = node.getInfo();
        if (r == null)
            return;
        int roomW = r.width;
        int roomH = r.height;
        int ox = (getWidth() - roomW) / 2;
        int oy = (getHeight() - roomH) / 2 - 20;
        g.translate(ox, oy);
        java.util.Random rnd = new java.util.Random();
        for (Crystal c : r.drops) {
            if (c.collected)
                continue;
            // Selección aleatoria de sprite
            BufferedImage[] crystalSprites = {
                    ResourceManager.c1,
                    ResourceManager.c2,
                    ResourceManager.c3,
                    ResourceManager.c4,
                    ResourceManager.c5
            };
            BufferedImage sprite = crystalSprites[rnd.nextInt(crystalSprites.length)];
            if (sprite != null) {
                int drawW = sprite.getWidth();
                int drawH = sprite.getHeight();
                int dx = (int) (c.x - drawW / 2f);
                int dy = (int) (c.y - drawH / 2f);
                g.drawImage(sprite, dx, dy, drawW, drawH, null);
            }
        }
        g.translate(-ox, -oy);
    }

    // Dibujar XP
    private void drawXpBar(Graphics2D g, int screenW, int screenH) {
        Player p = controller.player;
        int barH = 24;
        int margin = 8;
        int x = margin;
        int y = screenH - barH - margin;
        int w = screenW - margin * 2;
        g.setColor(new Color(40, 32, 24));
        g.fillRect(x, y, w, barH);

        float prog;
        if (p.xpToNextLevel > 0)
            prog = Math.max(0f, Math.min(1f, (float) p.currentXp / (float) p.xpToNextLevel));
        else
            prog = 0f;
        g.setColor(new Color(10, 163, 175));
        g.fillRect(x + 2, y + 2, Math.max(0, (int) ((w - 4) * prog)), barH - 4);
        g.setColor(Color.WHITE);
        g.drawString("Lv " + p.level + " XP: " + p.currentXp + "/" + p.xpToNextLevel, x + 8, y + barH - 6);
    }

    // Draw level-up modal using controller.getCurrentChoices()
    private void drawLevelUpModal(Graphics2D g, int screenW, int screenH) {
        List<Choice> choices = controller.getCurrentChoices();
        if (choices == null || choices.isEmpty())
            return;

        int mw = 600, mh = 240;
        int mx = (screenW - mw) / 2, my = (screenH - mh) / 2;
        g.setColor(new Color(20, 20, 20, 220));
        g.fillRect(mx, my, mw, mh);
        g.setColor(Color.WHITE);
        g.drawString("¡Elige una mejora!", mx + 16, my + 24);

        for (int i = 0; i < choices.size(); i++) {
            Choice c = choices.get(i);
            int ox = mx + 16 + i * (mw / 3);
            int oy = my + 40;
            int optionW = mw / 3 - 24, optionH = mh - 64;
            g.setColor(new Color(60, 60, 60));
            g.fillRect(ox, oy, optionW, optionH);
            g.setColor(Color.WHITE);
            g.drawString((i + 1) + ". " + c.name, ox + 8, oy + 18);
            g.drawString(c.description, ox + 8, oy + 36);
            if (c.kind == Choice.Kind.WEAPON) {
                int wl = controller.player.getWeaponLevel(c.id);
                g.drawString("Nivel actual: " + wl, ox + 8, oy + 56);
            } else {
                int st = controller.player.getPassiveStacks(c.id);
                g.drawString("Pilas: " + st, ox + 8, oy + 56);
            }
        }
    }

    // ------------- input listeners -------------
    @Override
    public void keyPressed(KeyEvent e) {
        int kc = e.getKeyCode();
        boolean changed = false;
        if (kc == KeyEvent.VK_W || kc == KeyEvent.VK_UP) {
            up = true;
            changed = true;
        }
        if (kc == KeyEvent.VK_S || kc == KeyEvent.VK_DOWN) {
            down = true;
            changed = true;
        }
        if (kc == KeyEvent.VK_A || kc == KeyEvent.VK_LEFT) {
            left = true;
            changed = true;
        }
        if (kc == KeyEvent.VK_D || kc == KeyEvent.VK_RIGHT) {
            right = true;
            changed = true;
        }
        if (changed)
            updatePlayerFacingFromKeys();
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int kc = e.getKeyCode();
        boolean changed = false;
        if (kc == KeyEvent.VK_W || kc == KeyEvent.VK_UP) {
            up = false;
            changed = true;
        }
        if (kc == KeyEvent.VK_S || kc == KeyEvent.VK_DOWN) {
            down = false;
            changed = true;
        }
        if (kc == KeyEvent.VK_A || kc == KeyEvent.VK_LEFT) {
            left = false;
            changed = true;
        }
        if (kc == KeyEvent.VK_D || kc == KeyEvent.VK_RIGHT) {
            right = false;
            changed = true;
        }
        if (changed)
            updatePlayerFacingFromKeys();
    }

    private void updatePlayerFacingFromKeys() {
        // construye vector según combinación de teclas; si ninguna, no cambia facing
        // (mantiene la última)
        float dx = 0f, dy = 0f;
        if (left)
            dx -= 1f;
        if (right)
            dx += 1f;
        if (up)
            dy -= 1f;
        if (down)
            dy += 1f;
        if (Math.abs(dx) < 1e-4f && Math.abs(dy) < 1e-4f) {
            // ninguna tecla presionada: no actualizar facing para conservar la última
            // dirección conocida
            return;
        }
        // actualizar facing en el player
        controller.player.setFacing(dx, dy);
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        mousePos = e.getPoint();
        if (controller.isLevelUpModalOpen()) {
            int screenW = getWidth(), screenH = getHeight();
            int mw = 600, mh = 240;
            int mx = (screenW - mw) / 2, my = (screenH - mh) / 2;
            int optionW = mw / 3 - 24;
            int oxBase = mx + 16;
            int oy = my + 40;
            List<Choice> choices = controller.getCurrentChoices();
            for (int i = 0; i < choices.size(); i++) {
                int ox = oxBase + i * (mw / 3);
                Rectangle optRect = new Rectangle(ox, oy, optionW, mh - 64);
                if (optRect.contains(mousePos)) {
                    controller.onLevelUpChoiceSelected(i);
                    return;
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mousePos = e.getPoint();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mousePos = e.getPoint();
    }

    private void drawKeys(Graphics2D g) {
        BinaryTreeNode<MineRoom> node = controller.nodoActual;
        if (node == null)
            return;
        MineRoom r = node.getInfo();
        if (r == null)
            return;
        int roomW = r.width;
        int roomH = r.height;
        int ox = (getWidth() - roomW) / 2;
        int oy = (getHeight() - roomH) / 2 - 20;
        g.translate(ox, oy);
        for (Key kd : r.keys) {
            if (kd == null || kd.collected)
                continue;
            int sx = (int) (kd.x);
            int sy = (int) (kd.y);
            BufferedImage sprite = ResourceManager.key;
            if (sprite != null) {
                int drawW = sprite.getWidth();
                int drawH = sprite.getHeight();
                int dx = sx - drawW / 2;
                int dy = sy - drawH / 2;
                g.drawImage(sprite, dx, dy, drawW, drawH, null);
            } else {
                // fallback: rectángulo dorado
                g.setColor(new Color(220, 200, 80));
                g.fillRect(sx - 8, sy - 6, 16, 12);
                g.setColor(new Color(120, 90, 20));
                g.drawRect(sx - 8, sy - 6, 16, 12);
            }
            // dibujar id corta encima
            g.setColor(Color.WHITE);
            g.drawString(kd.id != null ? kd.id : "key", sx - 8, sy - 10);
        }
        g.translate(-ox, -oy);
    }

    // ---------------- Pause / Resume / Input reset ----------------

    /** Pone todas las flags de teclado a false para evitar que queden atascadas */
    public void resetInputState() {
        try {
            java.lang.reflect.Field fUp = this.getClass().getDeclaredField("up");
            java.lang.reflect.Field fDown = this.getClass().getDeclaredField("down");
            java.lang.reflect.Field fLeft = this.getClass().getDeclaredField("left");
            java.lang.reflect.Field fRight = this.getClass().getDeclaredField("right");
            fUp.setAccessible(true);
            fDown.setAccessible(true);
            fLeft.setAccessible(true);
            fRight.setAccessible(true);
            fUp.setBoolean(this, false);
            fDown.setBoolean(this, false);
            fLeft.setBoolean(this, false);
            fRight.setBoolean(this, false);
        } catch (NoSuchFieldException nsf) {
            // si tus campos no se llaman así, setéalos manualmente:
            // this.up = this.down = this.left = this.right = false;
            up = down = left = right = false;
        } catch (Exception ex) {
            up = down = left = right = false;
        }
    }

    public void pause() {
        // notificar al controlador para bloquear updates externos cuanto antes
        try {
            controller.setExternallyPaused(true);
        } catch (Exception ex) {
            /* ignore */ }

        paused = true;
        try {
            if (timer != null && timer.isRunning())
                timer.stop();
        } catch (Throwable t) {
        }
        resetInputState();
        System.out.println("GamePanel: pause() ejecutado (paused=true, controlador notificado)");
    }

    /** Resume el GamePanel (reinicia Timer, resetea inputs y solicita foco) */
    /** Resume el GamePanel (reinicia Timer, resetea inputs y solicita foco) */
    public void resume() {
        // 1) levantar bandera interna
        paused = false;

        // 2) reiniciar timer para que el loop del panel vuelva a tener control
        try {
            if (timer != null && !timer.isRunning())
                timer.start();
        } catch (Throwable t) {
            t.printStackTrace();
        }

        // 3) reset de flags de entrada para evitar "stuck keys"
        resetInputState();

        // 4) notificar al controlador que puede aceptar updates externos (defensa en
        // profundidad)
        try {
            controller.setExternallyPaused(false);
        } catch (Exception ex) {
            // imprimir para depuración si no existe el método
            System.out.println("GamePanel.resume: no se pudo notificar a GameController (setExternallyPaused) - " + ex);
        }

        // 5) re-registrar KeyListener (protección si perdió vinculación) y Key Bindings
        // si usas
        try {
            this.removeKeyListener(this);
            this.addKeyListener(this);
        } catch (Exception ex) {
            // no crítico
        }

        // 6) limpiar focus owner global y pedir foco de forma asíncrona para que Swing
        // procese la eliminación del overlay primero
        try {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
        } catch (Exception ex) {
            /* ignore */ }

        SwingUtilities.invokeLater(() -> {
            // enviar KEY_RELEASED sintéticos para liberar cualquier tecla atascada
            int[] keys = new int[] {
                    KeyEvent.VK_W, KeyEvent.VK_A, KeyEvent.VK_S, KeyEvent.VK_D,
                    KeyEvent.VK_UP, KeyEvent.VK_LEFT, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT
            };
            long when = System.currentTimeMillis();
            for (int kc : keys) {
                try {
                    KeyEvent rel = new KeyEvent(this, KeyEvent.KEY_RELEASED, when, 0, kc, KeyEvent.CHAR_UNDEFINED);
                    Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(rel);
                } catch (Exception ex) {
                    /* ignore per key */ }
            }

            // pedir foco
            boolean focused = this.requestFocusInWindow();
            System.out.println("GamePanel.resume: requestFocusInWindow returned=" + focused);
        });

        // 7) logging final
        System.out.println("GamePanel: resume() ejecutado (paused=false, timerRunning="
                + (timer != null && timer.isRunning()) + ")");
    }

    /** Comprueba si está pausado (útil desde fuera) */
    public boolean isPaused() {
        return paused;
    }
}

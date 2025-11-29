package game;

import Tree.BinaryTree;
import Tree.BinaryTreeNode;
import Tree.TreeNode;
import effects.Effect;
import entities.BossEnemy;
import entities.Enemy;
import entities.EnemyManager;
import entities.Player;
import map.*;
import menu.Choice;
import menu.PerkPool;
import weapons.OrbittingOrb;
import weapons.Projectile;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

public class GameController {

    public final BinaryTree<MineRoom> map;
    public BinaryTreeNode<MineRoom> nodoActual;
    public final Player player;
    public final EnemyManager enemyManager;
    public final PerkPool pool;
    // diagnóstico: evita spam imprimiendo la traza solo la primera vez durante
    // pausa
    private final java.util.concurrent.atomic.AtomicBoolean _loggedUpdateWhilePaused = new java.util.concurrent.atomic.AtomicBoolean(
            false);

    // temporales visuales / físicas
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Effect> effects = new ArrayList<>();

    // nivelación UI
    private boolean levelUpModalOpen = false;
    private final Queue<Integer> pendingLevelUps = new ArrayDeque<>();
    private List<Choice> currentChoices = new ArrayList<>();

    // random
    private final Random rnd = new Random();

    // configuración de balance (ajustable)
    private final int BASE_SPAWN = 15;
    private final float SCALE_PER_CLEAR = 0.5f; // cada clear aumenta en +50%

    // evento UI
    private GameEventListener eventListener;

    public GameController(BinaryTree<MineRoom> map, BinaryTreeNode<MineRoom> start) {
        this.map = map;
        // si start es null o es la raíz, intentar usar una hoja aleatoria
        BinaryTreeNode<MineRoom> resolvedStart = start;
        try {
            if (resolvedStart == null) {
                resolvedStart = SimpleMapBuilder.pickRandomLeaf(map);
            } else {
                TreeNode<MineRoom> root = map != null ? map.getRoot() : null;
                if (root instanceof BinaryTreeNode && ((BinaryTreeNode<MineRoom>) root).equals(resolvedStart)) {
                    BinaryTreeNode<MineRoom> leaf = SimpleMapBuilder.pickRandomLeaf(map);
                    if (leaf != null)
                        resolvedStart = leaf;
                }
            }
        } catch (Exception ex) {
            // si algo falla, mantenemos el start original (fallback)
            resolvedStart = start;
        }
        this.nodoActual = resolvedStart;
        this.pool = new PerkPool();
        MineRoom r = nodoActual != null ? nodoActual.getInfo() : null;
        if (r == null) {
            r = new MineRoom(0, 800, 600);
            this.nodoActual = new BinaryTreeNode<>(r);
        }
        this.player = new Player(r.width / 2f, r.height - 60f, pool);
        this.enemyManager = new EnemyManager(map);

        player.recomputeDerivedStats();
        onPlayerEnter(nodoActual);
    }

    // -------------------------
    // EventListener
    // -------------------------
    public void setGameEventListener(GameEventListener l) {
        this.eventListener = l;
    }

    private void notifyWin() {
        System.out.println("¡Has alcanzado la salida! Victoria.");
        if (eventListener != null) {
            SwingUtilities.invokeLater(() -> eventListener.onWin());
        }
    }

    private void notifyGameOver() {
        System.out.println("Game Over - El jugador ha sido derrotado.");
        if (eventListener != null) {
            SwingUtilities.invokeLater(() -> eventListener.onGameOver());
        }
    }

    // -------------------------
    // Update loop
    // -------------------------
    public void update(float dt, List<Rectangle> roomWalls) {
        // si modal abierto, solo animar effects (opcional)
        // defensa: si la pausa fue activada externamente, no ejecutar update
        // defensa + diagnóstico: bloquear updates externos cuando el controlador está
        // marcado en pausa
        if (this.externallyPaused) {
            // imprimir la pila una sola vez para localizar el llamador externo
            if (_loggedUpdateWhilePaused.compareAndSet(false, true)) {
                System.out.println(
                        "game.GameController.update called while externallyPaused==true — stacktrace to locate caller:");
                Thread.dumpStack();
            }
            return;
        }

        if (levelUpModalOpen) {
            updateEffects(dt);
            return;
        }

        MineRoom room = nodoActual != null ? nodoActual.getInfo() : null;
        if (room == null)
            return;

        // puertas (transición)
        for (Door door : new ArrayList<>(room.doors)) {
            if (player.getBounds().intersects(door.area) && !door.locked) {
                // si la puerta es la de victoria, notificar victoria
                if (door.isWin) {
                    notifyWin();
                    return;
                } else {
                    transitionTo(door);
                    return;
                }
            }
        }

        // actualizar projectiles (pueden dañar y expirar)
        updateProjectiles(dt);

        // actualizar enemigos
        List<Enemy> enemies = new ArrayList<>(enemyManager.getEnemiesAt(nodoActual));
        for (Enemy e : enemies)
            e.update(dt, player, room);

        // colisiones
        resolveEnemyEnemyCollisions(enemies, dt);
        resolveEnemyPlayerCollisions(enemies, dt);

        // actualizar jugador (disparos)
        player.update(dt, enemies, this);

        // check if player is dead
        if (!player.alive) {
            notifyGameOver();
            return;
        }

        // recolección de cristales y llaves
        collectCrystals(room);
        collectKeys(room);

        // procesar muertes: si enemies murieron durante update, onEnemyKilled debe
        // haber sido llamado por entities.Enemy
        // ahora comprobamos si la sala está limpia y manejamos clearCount (solo una vez
        // por transición a limpia)
        handleRoomClearedIfNeeded(nodoActual);

        // spawns: si sala limpia, spawnear nueva oleada escalada
        if (enemyManager.isCleared(nodoActual)) {
            // prevenir spawns en la raíz si hay jefe (policy opcional)
            if (!isRootWithActiveBoss(nodoActual)) {
                int baseAmount = calculateSpawnAmountForNode(nodoActual);
                spawnHordeScaled(nodoActual, baseAmount, Math.max(room.width, room.height) * 0.9f);
            }
        }

        // actualizar efectos visuales (explosiones, slashes)
        updateEffects(dt);
    }

    // -------------------------
    // Projectiles / Effects
    // -------------------------
    public void spawnProjectile(Projectile p) {
        if (p == null)
            return;
        projectiles.add(p);
    }

    public void spawnEffect(Effect e) {
        if (e == null)
            return;
        effects.add(e);
    }

    private void updateProjectiles(float dt) {
        if (projectiles.isEmpty())
            return;
        List<Projectile> alive = new ArrayList<>();
        for (Projectile p : projectiles) {
            p.update(dt, this);
            if (!p.isExpired())
                alive.add(p);
        }
        projectiles.clear();
        projectiles.addAll(alive);
    }

    private void updateEffects(float dt) {
        if (effects.isEmpty())
            return;
        List<Effect> alive = new ArrayList<>();
        for (Effect ef : effects) {
            ef.update(dt, this);
            if (!ef.isExpired())
                alive.add(ef);
        }
        effects.clear();
        effects.addAll(alive);
    }

    public void renderProjectiles(Graphics g, int screenW, int screenH) {
        MineRoom r = nodoActual != null ? nodoActual.getInfo() : null;
        if (r == null)
            return;
        int ox = (screenW - r.width) / 2;
        int oy = (screenH - r.height) / 2 - 20;
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(ox, oy);
        for (Projectile p : projectiles)
            p.render(g2);
        for (Effect ef : effects)
            ef.render(g2);
        g2.translate(-ox, -oy);
        g2.dispose();
    }

    // -------------------------
    // Recolección -> XP
    // -------------------------
    private void collectCrystals(MineRoom room) {
        if (room == null)
            return;
        List<Crystal> toRemove = new ArrayList<>();
        for (Crystal c : new ArrayList<>(room.drops)) {
            if (c.collected)
                continue;
            float dx = player.x - c.x;
            float dy = player.y - c.y;
            if (dx * dx + dy * dy < 20f * 20f) {
                c.collected = true;
                toRemove.add(c);
                player.addXp(c.value, this);
            }
        }
        room.drops.removeAll(toRemove);
    }

    // -------------------------
    // Nivelación / modal
    // -------------------------
    public void onPlayerLeveledUp(int newLevel) {
        pendingLevelUps.add(newLevel);
        if (!levelUpModalOpen)
            openNextLevelUpModal();
    }

    private void openNextLevelUpModal() {
        if (pendingLevelUps.isEmpty()) {
            levelUpModalOpen = false;
            return;
        }
        int lvl = pendingLevelUps.poll();
        levelUpModalOpen = true;
        currentChoices = pool.pickOptions(3, player);
        System.out.println("Subiste a nivel " + lvl + ". Elige una mejora:");
        for (int i = 0; i < currentChoices.size(); i++) {
            Choice c = currentChoices.get(i);
            System.out.println((i + 1) + ") " + c.name + " - " + c.description);
        }
    }

    public boolean isLevelUpModalOpen() {
        return levelUpModalOpen;
    }

    public List<Choice> getCurrentChoices() {
        return currentChoices;
    }

    public void onLevelUpChoiceSelected(int index) {
        if (!levelUpModalOpen)
            return;
        if (index < 0 || index >= currentChoices.size())
            return;
        Choice choice = currentChoices.get(index);
        player.applyChoice(choice);
        player.recomputeDerivedStats();
        levelUpModalOpen = false;
        currentChoices = new ArrayList<>();
        if (!pendingLevelUps.isEmpty())
            openNextLevelUpModal();
    }

    // -------------------------
    // entities.Enemy killed: drop crystals escalados
    // -------------------------
    public void onEnemyKilled(Enemy e) {
        if (e == null)
            return;

        // intentar obtener la sala donde estaba el enemigo, si entities.Enemy tiene
        // getNode() usarlo; si no fallback a nodoActual
        BinaryTreeNode<MineRoom> node = nodoActual;
        try {
            java.lang.reflect.Method m = e.getClass().getMethod("getNode");
            Object nodeObj = m.invoke(e);
            if (nodeObj instanceof BinaryTreeNode)
                node = (BinaryTreeNode<MineRoom>) nodeObj;
        } catch (Exception ex) {
            // fallback: no hay getNode(), usamos nodoActual
        }

        if (node == null)
            node = nodoActual;
        if (node == null) {
            enemyManager.removeEnemy(e);
            return;
        }

        // remove enemy from manager
        enemyManager.removeEnemy(e);

        // drop crystals in that room
        MineRoom room = node.getInfo();
        if (room == null)
            return;

        // --- SPECIAL: Si es un BossEnemy, dropear la llave dorada en una sala
        // aleatoria ---
        if (e instanceof BossEnemy) {
            BinaryTreeNode<MineRoom> randomNode = SimpleMapBuilder.pickRandomNonRootNode(map);
            if (randomNode != null) {
                MineRoom randomRoom = randomNode.getInfo();
                if (randomRoom != null) {
                    // Posición aleatoria dentro de la sala
                    int margin = 60;
                    float kx = margin + rnd.nextFloat() * Math.max(1, randomRoom.width - margin * 2);
                    float ky = margin + rnd.nextFloat() * Math.max(1, randomRoom.height - margin * 2);
                    Key goldenKey = new Key(kx, ky, "golden-key");
                    randomRoom.keys.add(goldenKey);
                    System.out.println("¡El jefe ha sido derrotado! La llave dorada aparece en una sala aleatoria (ID: "
                            + randomRoom.id + ").");
                }
            }
        }

        int basePerLevel = 4; // base gems per enemy level
        int variance = 2;
        int clearMultiplier = 1 + Math.max(0, room.clearCount); // 1 for initial clearCount==0, then increases
        int numCrystals = Math.max(1, (basePerLevel * Math.max(1, e.level) * clearMultiplier) / 2);

        float ex = e.getX();
        float ey = e.getY();

        for (int i = 0; i < numCrystals; i++) {
            float rx = ex + (rnd.nextFloat() - 0.5f) * 30f;
            float ry = ey + (rnd.nextFloat() - 0.5f) * 30f;
            int value = Math.max(1, basePerLevel + (e.level - 1) * 2 + rnd.nextInt(variance + 1));
            Crystal c = new Crystal(rx, ry, value);
            room.drops.add(c);
        }
    }

    // alias si otros módulos esperan este método
    public void onEnemyKilledPublic(Enemy e) {
        onEnemyKilled(e);
    }

    // -------------------------
    // Room clear handling and scaled spawns
    // -------------------------
    private void handleRoomClearedIfNeeded(BinaryTreeNode<MineRoom> node) {
        if (node == null)
            return;
        MineRoom room = node.getInfo();
        if (room == null)
            return;

        boolean cleared = enemyManager.isCleared(node);
        if (!cleared) {
            // room is occupied now; reset wasCleared so next vacancy counts
            room.wasCleared = false;
            return;
        }

        // if cleared and wasn't marked previously this frame, increment clearCount
        if (!room.wasCleared) {
            room.wasCleared = true;
            room.clearCount = Math.max(0, room.clearCount) + 1;
            // optional: reward bonus XP/crystals on clear event
            int bonus = 2 + room.clearCount / 2;
            for (int i = 0; i < bonus; i++) {
                float rx = room.width / 2f + (rnd.nextFloat() - 0.5f) * 48f;
                float ry = room.height / 2f + (rnd.nextFloat() - 0.5f) * 48f;
                int val = 4 + rnd.nextInt(3) + room.clearCount;
                room.drops.add(new Crystal(rx, ry, val));
            }
        }
    }

    private boolean isRootWithActiveBoss(BinaryTreeNode<MineRoom> node) {
        if (node == null || map == null || map.getRoot() == null)
            return false;
        BinaryTreeNode<MineRoom> root = (BinaryTreeNode<MineRoom>) map.getRoot();
        if (!root.equals(node))
            return false;
        // check for boss alive in this node
        List<Enemy> list = enemyManager.getEnemiesAt(node);
        for (Enemy e : list) {
            if (e instanceof BossEnemy && e.isAlive())
                return true;
        }
        return false;
    }

    private int calculateSpawnAmountForNode(BinaryTreeNode<MineRoom> node) {
        int nivel = 0;
        try {
            nivel = Math.max(0, map.nodeLevel(node));
        } catch (Exception ex) {
            nivel = 0;
        }
        int altura = 1;
        try {
            altura = Math.max(1, map.treeHeight());
        } catch (Exception ex) {
            altura = 1;
        }
        double factor = 1.0 - (double) nivel / altura;
        int base = BASE_SPAWN;
        int maxExtra = 18;
        return base + (int) Math.round(maxExtra * factor);
    }

    // spawn scaled by room.clearCount
    private void spawnHordeScaled(BinaryTreeNode<MineRoom> node, int baseAmount, float spawnDistance) {
        if (node == null || baseAmount <= 0)
            return;
        MineRoom r = node.getInfo();
        if (r == null)
            return;

        int clearTimes = Math.max(0, r.clearCount);
        int amount = baseAmount + Math.round(baseAmount * SCALE_PER_CLEAR * clearTimes);
        int maxAmount = Math.max(30, baseAmount * 3);
        amount = Math.min(amount, maxAmount);

        spawnHordeInternal(node, amount, spawnDistance);
    }

    // actual spawn impl (similar a la anterior lógica)
    private void spawnHordeInternal(BinaryTreeNode<MineRoom> node, int amount, float spawnDistance) {
        if (node == null || amount <= 0)
            return;
        MineRoom r = node.getInfo();
        if (r == null)
            return;

        float cx = r.width / 2f;
        float cy = r.height / 2f;
        float dist = Math.max(48f, Math.min(spawnDistance, Math.max(r.width, r.height)));

        List<Enemy> spawnList = new ArrayList<>();
        for (int i = 0; i < amount; i++) {
            double ang = rnd.nextDouble() * Math.PI * 2.0;
            double rad = dist * (0.8 + rnd.nextDouble() * 0.4);
            float sx = cx + (float) Math.cos(ang) * (float) rad;
            float sy = cy + (float) Math.sin(ang) * (float) rad;

            if (sx >= 0 && sx <= r.width && sy >= 0 && sy <= r.height) {
                float dl = Math.abs(sx - 0);
                float dr = Math.abs(sx - r.width);
                float dt = Math.abs(sy - 0);
                float db = Math.abs(sy - r.height);
                float min = Math.min(Math.min(dl, dr), Math.min(dt, db));
                float pad = Math.max(40f, Math.min(120f, dist * 0.4f));
                if (min == dl)
                    sx = -pad;
                else if (min == dr)
                    sx = r.width + pad;
                else if (min == dt)
                    sy = -pad;
                else
                    sy = r.height + pad;
            }

            // scale enemy level by subtree height (approx) to diversify
            int subtreeH = subtreeHeight(node);
            int maxLevel = Math.max(1, subtreeH + 1);
            int level = 1 + rnd.nextInt(maxLevel);
            int baseHp = 20 + level * 8;
            float baseSpeed = 20f + level * 4f;
            Enemy e = new Enemy(node, sx, sy, baseHp, baseSpeed, level);
            spawnList.add(e);
        }

        for (Enemy e : spawnList)
            enemyManager.addEnemyAt(node, e);
    }

    // helper subtree height
    private int subtreeHeight(BinaryTreeNode<MineRoom> node) {
        if (node == null)
            return -1;
        BinaryTreeNode<MineRoom> left = node.getLeft();
        BinaryTreeNode<MineRoom> right = node.getRight();
        if (left == null && right == null)
            return 0;
        int hl = left != null ? subtreeHeight(left) : -1;
        int hr = right != null ? subtreeHeight(right) : -1;
        return 1 + Math.max(hl, hr);
    }

    // -------------------------
    // Transition (puertas)
    // -------------------------
    private void transitionTo(Door door) {
        if (door == null)
            return;

        if (door.isWin) {
            if (door.locked) {
                System.out.println("La puerta está cerrada. Necesitas la llave.");
                return;
            } else {
                notifyWin();
                return;
            }
        }

        BinaryTreeNode<MineRoom> origenNode = this.nodoActual;
        MineRoom origenRoom = origenNode != null ? origenNode.getInfo() : null;

        BinaryTreeNode<MineRoom> destinoNode = door.destino;
        if (destinoNode == null)
            return;
        MineRoom destRoom = destinoNode.getInfo();
        if (destRoom == null)
            return;

        Door mirror = null;
        for (Door d : destRoom.doors) {
            if (d == null)
                continue;
            if (d.destino != null && origenNode != null && d.destino.equals(origenNode)) {
                mirror = d;
                break;
            }
        }

        float spawnX = door.spawnX;
        float spawnY = door.spawnY;
        int offset = 36;

        if (mirror != null) {
            Rectangle a = mirror.area;
            float cx = a.x + a.width / 2f;
            float cy = a.y + a.height / 2f;
            int distLeft = Math.abs(Math.round(cx - 0));
            int distRight = Math.abs(Math.round(cx - destRoom.width));
            int distTop = Math.abs(Math.round(cy - 0));
            int distBottom = Math.abs(Math.round(cy - destRoom.height));
            int min = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));

            if (min == distLeft) {
                spawnX = Math.min(destRoom.width - 16, 16 + offset);
                spawnY = clamp(cy, 16f, destRoom.height - 16f);
            } else if (min == distRight) {
                spawnX = Math.max(16, destRoom.width - 16 - offset);
                spawnY = clamp(cy, 16f, destRoom.height - 16f);
            } else if (min == distTop) {
                spawnX = clamp(cx, 16f, destRoom.width - 16f);
                spawnY = Math.min(destRoom.height - 16, 16 + offset);
            } else {
                spawnX = clamp(cx, 16f, destRoom.width - 16f);
                spawnY = Math.max(16, destRoom.height - 16 - offset);
            }
        } else {
            int doorCenterX = door.area.x + door.area.width / 2;
            int doorCenterY = door.area.y + door.area.height / 2;
            int distLeft = origenRoom != null ? Math.abs(doorCenterX - 0) : Integer.MAX_VALUE;
            int distRight = origenRoom != null ? Math.abs(doorCenterX - origenRoom.width) : Integer.MAX_VALUE;
            int distTop = origenRoom != null ? Math.abs(doorCenterY - 0) : Integer.MAX_VALUE;
            int distBottom = origenRoom != null ? Math.abs(doorCenterY - origenRoom.height) : Integer.MAX_VALUE;
            int min = Math.min(Math.min(distLeft, distRight), Math.min(distTop, distBottom));

            float midW = destRoom.width / 2f;
            float midH = destRoom.height / 2f;

            if (min == distLeft) {
                spawnX = Math.max(16, offset);
                spawnY = clamp(midH, 16f, destRoom.height - 16f);
            } else if (min == distRight) {
                spawnX = destRoom.width - Math.max(16, offset);
                spawnY = clamp(midH, 16f, destRoom.height - 16f);
            } else if (min == distTop) {
                spawnX = clamp(midW, 16f, destRoom.width - 16f);
                spawnY = Math.max(16, offset);
            } else {
                spawnX = clamp(midW, 16f, destRoom.width - 16f);
                spawnY = destRoom.height - Math.max(16, offset);
            }
        }

        this.nodoActual = destinoNode;
        this.player.x = spawnX;
        this.player.y = spawnY;

        onPlayerEnter(nodoActual);
    }

    private float clamp(float v, float min, float max) {
        if (v < min)
            return min;
        if (v > max)
            return max;
        return v;
    }

    // -------------------------
    // onPlayerEnter: spawn boss in root or initial horde
    // -------------------------
    public void onPlayerEnter(BinaryTreeNode<MineRoom> node) {
        if (node == null)
            return;
        MineRoom r = node.getInfo();
        if (r == null || r.colapsado)
            return;

        BinaryTreeNode<MineRoom> rootT = (BinaryTreeNode<MineRoom>) map.getRoot();
        if (rootT != null && node.equals(rootT)) {
            // spawn boss if not present
            boolean bossExists = false;
            List<Enemy> existing = enemyManager.getEnemiesAt(node);
            for (Enemy e : existing) {
                if (e instanceof BossEnemy && e.isAlive()) {
                    bossExists = true;
                    break;
                }
            }
            if (!bossExists) {
                float sx = r.width / 2f;
                float sy = r.height / 2f;
                BossEnemy boss = new BossEnemy(node, sx, sy, 1, 1f, 1);
                enemyManager.addEnemyAt(node, boss);
                System.out.println("Jefe final ha aparecido en la raíz.");
            }
            return;
        }

        List<Enemy> existing = enemyManager.getEnemiesAt(node);
        if (existing.isEmpty()) {
            int amount = calculateSpawnAmountForNode(node);
            spawnHordeScaled(node, amount, Math.max(r.width, r.height) * 0.9f);
        }

        // opcional: listar keys en la sala al entrar (útil para debug)
        for (Key k : r.keys) {
            if (k != null && !k.collected) {
                System.out.println("Sala " + r.id + " contiene key " + k.id + " en " + k.x + "," + k.y);
            }
        }
    }

    // -------------------------
    // Utility and collision placeholders
    // -------------------------
    public void onEnemyKilledByExternal(Enemy e) {
        onEnemyKilled(e);
    } // alias if needed

    // Placeholder simple collision resolvers (tweak to your version)
    private void resolveEnemyEnemyCollisions(List<Enemy> enemies, float dt) {
        int n = enemies.size();
        for (int i = 0; i < n; i++) {
            Enemy a = enemies.get(i);
            if (!a.isAlive())
                continue;
            for (int j = i + 1; j < n; j++) {
                Enemy b = enemies.get(j);
                if (!b.isAlive())
                    continue;
                float dx = b.getX() - a.getX();
                float dy = b.getY() - a.getY();
                float dist2 = dx * dx + dy * dy;
                float minDist = 18f;
                if (dist2 > 0f && dist2 < minDist * minDist) {
                    float dist = (float) Math.sqrt(dist2);
                    float overlap = (minDist - dist);
                    float nx = dx / (dist + 1e-6f);
                    float ny = dy / (dist + 1e-6f);
                    float push = 0.5f * overlap;
                    a.translate(-nx * push, -ny * push);
                    b.translate(nx * push, ny * push);
                }
            }
        }
    }

    private void resolveEnemyPlayerCollisions(List<Enemy> enemies, float dt) {
        for (Enemy e : enemies) {
            if (!e.isAlive())
                continue;
            if (e.intersectsPlayer(player)) {
                player.receiveDamage(e.contactDamage);
                float dx = player.x - e.getX();
                float dy = player.y - e.getY();
                float d2 = dx * dx + dy * dy;
                if (d2 < 0.0001f) {
                    dx = 0.5f;
                    dy = 0.5f;
                    d2 = dx * dx + dy * dy;
                }
                float d = (float) Math.sqrt(d2);
                float nx = dx / d;
                float ny = dy / d;
                float basePlayerPush = 60f;
                player.applyKnockback(nx * basePlayerPush * (1f - 0.12f * e.level),
                        ny * basePlayerPush * (1f - 0.12f * e.level));
                float baseEnemyPush = 30f;
                e.applyKnockback(-nx * baseEnemyPush * (1f - 0.08f * e.level),
                        -ny * baseEnemyPush * (1f - 0.08f * e.level));
                player.x += nx * 1.5f;
                player.y += ny * 1.5f;
                e.translate(-nx * 1.5f, -ny * 1.5f);
            }
        }
    }

    // -------------------------
    // Miscellaneous and utilities
    // -------------------------
    public void collapseNode(BinaryTreeNode<MineRoom> node) {
        if (node == null)
            return;
        MineRoom room = node.getInfo();
        if (room != null)
            room.colapsado = true;
        List<Enemy> enemies = new ArrayList<>(enemyManager.getEnemiesAt(node));
        for (Enemy en : enemies) {
            en.damage(9999, this);
            enemyManager.removeEnemy(en);
        }
        map.deleteNode(node);
        enemyManager.removeAllAt(node);
        if (nodoActual != null && nodoActual.equals(node)) {
            BinaryTreeNode<MineRoom> leaf = SimpleMapBuilder.pickRandomLeaf(map);
            if (leaf != null) {
                nodoActual = leaf;
                MineRoom rr = leaf.getInfo();
                player.x = rr.width / 2f;
                player.y = rr.height / 2f;
                onPlayerEnter(nodoActual);
            } else {
                System.out.println("Todas las salas colapsaron. Juego terminado.");
            }
        }
    }

    // Si otra parte del código necesita notificar victoria manualmente
    public void onWin() {
        notifyWin();
    }

    /**
     * Detecta pickup de llaves en la sala y las incorpora al inventario del jugador
     */
    private void collectKeys(MineRoom room) {
        if (room == null)
            return;
        java.util.List<Key> toRemove = new java.util.ArrayList<>();
        for (Key k : new java.util.ArrayList<>(room.keys)) {
            if (k == null || k.collected)
                continue;
            float dx = player.x - k.x;
            float dy = player.y - k.y;
            if (dx * dx + dy * dy < 20f * 20f) { // distancia de recogida (ajustable)
                k.collected = true;
                toRemove.add(k);
                // añadir al inventario lógico del jugador
                player.addKey(k.id);
                // opcional: también registrar id en room.keysInRoom si necesitas lógica de
                // persistencia
                room.addKeyToRoom(k.id);
                // evento/feedback
                onPlayerPickedKey(k.id, room);
            }
        }
        // eliminar físicamente las keys colectadas de la sala
        for (Key k : toRemove) {
            // quitar por id para evitar problemas de igualdad de referencias
            room.removeKeyObjectById(k.id);
        }
    }

    /**
     * Hook que puedes usar para reproducir sonido, mostrar toast, o desbloquear
     * cosas
     */
    private void onPlayerPickedKey(String keyId, MineRoom room) {
        if (keyId == null)
            return;
        System.out.println("Llave recogida: " + keyId);
        // ejemplo: si la llave recogida es la 'golden-key' y la raíz tiene una puerta
        // bloqueada, la desbloqueamos
        try {
            BinaryTreeNode<MineRoom> root = (BinaryTreeNode<MineRoom>) map.getRoot();
            if (root != null) {
                MineRoom rootRoom = root.getInfo();
                if (rootRoom != null) {
                    for (Door d : rootRoom.doors) {
                        if (d != null && d.isWin && d.locked && "golden-key".equals(keyId)) {
                            d.locked = false;
                            System.out.println("Puerta de salida desbloqueada por golden-key.");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // silenciar si no aplica
        }
    }

    // contar orbes orbitantes activos por jugador (usa el nombre de clase
    // weapons.OrbittingOrb según tu código)
    public int countOrbitingOrbsForPlayer(Player p) {
        if (p == null)
            return 0;
        int c = 0;
        for (Effect ef : effects) {
            if (ef instanceof OrbittingOrb) {
                OrbittingOrb o = (OrbittingOrb) ef;
                if (!o.isExpired() && o.ownerPlayer == p)
                    c++;
            }
        }
        return c;
    }

    // detener y limpiar si es necesario (opcional helper usado por UI)
    public void shutdown() {
        // limpiar listas para evitar referencias retenidas
        projectiles.clear();
        effects.clear();

    }

    // campo (añadir cerca de otros campos volátiles)
    private volatile boolean externallyPaused = false;

    // setter público
    public void setExternallyPaused(boolean v) {
        this.externallyPaused = v;
    }

    // getter opcional
    public boolean isExternallyPaused() {
        return this.externallyPaused;
    }

}

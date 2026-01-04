package entities;

import Tree.BinaryTreeNode;
import game.GameController;
import map.Crystal;
import map.MineRoom;

import java.util.Random;

public class Enemy {
    private final BinaryTreeNode<MineRoom> node;
    private float x, y;
    protected float vx = 0f;
    protected float vy = 0f;
    protected float speed;
    protected int hp;
    protected int maxHp;
    private final int id;
    private boolean alive = true;
    private static int NEXT_ID = 1;
    private final Random rnd = new Random();
    public final int level;
    public final int contactDamage;
    protected float knockbackResistance;

    private boolean facingLeft = true;
    private int animTick = 0;

    public Enemy(BinaryTreeNode<MineRoom> node, float startX, float startY, int baseHp, float baseSpeed, int level) {
        this.node = node;
        this.x = startX;
        this.y = startY;
        this.level = Math.max(1, level);
        this.maxHp = Math.max(1, baseHp + (this.level - 1) * 12);
        this.hp = maxHp;
        this.speed = baseSpeed + 20 + (this.level - 1) * 6f;
        this.contactDamage = 6 + (this.level - 1) * 4;
        this.knockbackResistance = Math.min(0.9f, 0.2f + 0.15f * (this.level - 1));
        this.id = NEXT_ID++;
    }

    public BinaryTreeNode<MineRoom> getNode() {
        return node;
    }
    public int getHp() {
        return hp;
    }
    public int getMaxHp() {
        return maxHp;
    }
    public int getId() {
        return id;
    }
    public boolean isAlive() {
        return alive && hp > 0;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }
    public int getLevel() {
        return level;
    }
    public boolean isFacingLeft() {
        return facingLeft;
    }
    public int getAnimTick() {
        return animTick;
    }

    public void damage(int d, GameController controller) {
        if (!isAlive()) {
            return;
        }
        hp -= d;
        if (hp <= 0) {
            alive = false;
            die(controller);
        }
    }

    private void die(GameController controller) {
        MineRoom room = node.getInfo();
        if (room != null) {
            int drops = 1 + rnd.nextInt(3);
            for (int i = 0; i < drops; i++) {
                float ox = (rnd.nextFloat() - 0.5f) * 40f;
                float oy = (rnd.nextFloat() - 0.5f) * 40f;
                Crystal c = new Crystal(clampX(room, x + ox), clampY(room, y + oy), 1);
                room.drops.add(c);
            }
        }
        controller.onEnemyKilled(this);
    }

    public void applyKnockback(float impulseX, float impulseY) {
        vx += impulseX * (1f - knockbackResistance);
        vy += impulseY * (1f - knockbackResistance);
    }

    public void applyVelocity(float ax, float ay) {
        vx += ax;
        vy += ay;
    }

    public void translate(float dx, float dy) {
        x += dx;
        y += dy;
    }

    public void update(float dt, Player player, MineRoom room) {
        if (!isAlive()) {
            x += vx * dt;
            y += vy * dt;
            vx *= Math.max(0f, 1f - 6f * dt);
            vy *= Math.max(0f, 1f - 6f * dt);
            return;
        }
        if (room == null) {
            return;
        }

        float dx = player.x - x;
        float dy = player.y - y;
        float dist2 = dx * dx + dy * dy;
        if (dist2 > 0.0001f) {
            float dist = (float) Math.sqrt(dist2);
            float nx = dx / dist;
            float ny = dy / dist;
            x += (nx * speed + vx) * dt;
            y += (ny * speed + vy) * dt;

            if (nx < 0) {
                facingLeft = true;
            }
            else {
                facingLeft = false;
            }
        }
        else {
            x += vx * dt;
            y += vy * dt;
        }

        vx *= Math.max(0f, 1f - 6f * dt);
        vy *= Math.max(0f, 1f - 6f * dt);

        clampIfNearRoom(room);
        animTick++;
    }

    public boolean intersectsPlayer(Player p) {
        float dx = p.x - x;
        float dy = p.y - y;
        float r = 14f;
        boolean resultado;
        if (dx * dx + dy * dy <= r * r) {
            resultado = true;
        }
        else {
            resultado = false;
        }
        return resultado;
    }

    protected void clampIfNearRoom(MineRoom r) {
        float half = 8f;
        if (x < half) {
            x = half;
        }
        if (x > r.width - half) {
            x = r.width - half;
        }
        if (y < half) {
            y = half;
        }
        if (y > r.height - half) {
            y = r.height - half;
        }
    }

    private float clampX(MineRoom r, float vx) {
        float half = 8f;
        if (vx < half) {
            return half;
        }
        if (vx > r.width - half) {
            return r.width - half;
        }
        return vx;
    }

    private float clampY(MineRoom r, float vy) {
        float half = 8f;
        if (vy < half) {
            return half;
        }
        if (vy > r.height - half) {
            return r.height - half;
        }
        return vy;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }
}

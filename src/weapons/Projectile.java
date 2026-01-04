package weapons;

import Tree.BinaryTreeNode;
import entities.Enemy;
import entities.Player;
import game.GameController;
import map.MineRoom;

import java.awt.*;
import java.util.List;

public abstract class Projectile {
    public float x, y;
    public float vx, vy;
    protected float life;
    protected boolean expired = false;
    public final BinaryTreeNode<MineRoom> roomNode;
    public final GameController controller;
    public final Player owner;
    public final int damage;

    public Projectile(Player owner, BinaryTreeNode<MineRoom> roomNode, float x, float y, float vx, float vy, float lifeSeconds, int damage, GameController controller) {
        this.owner = owner;
        this.roomNode = roomNode;
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = lifeSeconds;
        this.damage = damage;
        this.controller = controller;
    }

    public boolean isExpired() {
        return this.expired;
    }

    protected void expire() {
        expired = true;
    }

    public void update(float dt, GameController ctx) {
        if (expired) {
            return;
        }
        life -= dt;
        if (life <= 0f) {
            expire();
            return;
        }
        x += vx * dt;
        y += vy * dt;
    }

    public abstract void render(Graphics2D g);

    protected void damageEnemiesInRadius(float cx, float cy, float radius, int dmg) {
        if (roomNode == null) {
            return;
        }
        List<Enemy> enemies = controller.enemyManager.getEnemiesAt(roomNode);
        float r2 = radius * radius;
        for (Enemy e : enemies) {
            if (!e.isAlive()) {
                continue;
            }
            float dx = e.getX() - cx;
            float dy = e.getY() - cy;
            float distanceSquared = dx * dx + dy * dy;
            if (distanceSquared <= r2) {
                e.damage(dmg, controller);
                float dist = (float)Math.sqrt(distanceSquared);
                float nx = 0f;
                float ny = -1f;
                if (dist > 0.001f) {
                    nx = dx / dist;
                    ny = dy / dist;
                }
                e.applyKnockback(nx * 80f, ny * 80f);
            }
        }
    }
}